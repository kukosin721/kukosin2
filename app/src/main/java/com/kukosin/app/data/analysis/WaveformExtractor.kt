package com.kukosin.app.data.analysis

import android.content.Context
import android.media.AudioFormat
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import com.kukosin.app.data.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.ByteOrder
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Extrae la forma de onda REAL de la canción (no random).
 *
 * - Una canción silenciosa → barras planas (RMS ≈ 0)
 * - Una con dinámica → picos reales donde hay energía
 * - Cada barra = RMS de un segmento del archivo (no 3 puntos como en PcmDecoder.capture)
 *
 * Cache en memoria para no re-decodificar la misma canción.
 * Si falla (YTM sin archivo, permiso, codec), devuelve null y la UI usa fallback.
 */
object WaveformExtractor {

    private const val BARS_DEFAULT = 180
    private const val TIMEOUT_US = 10_000L

    // Cache simple: uri string -> waveform normalizada 0..1
    private val cache = linkedMapOf<String, List<Float>>()
    private const val MAX_CACHE = 40

    suspend fun getWaveform(
        context: Context,
        track: Track,
        bars: Int = BARS_DEFAULT
    ): List<Float>? = withContext(Dispatchers.IO) {
        // Solo archivos locales tienen PCM accesible
        if (!track.isLocal) return@withContext null

        val key = track.uri.toString()
        synchronized(cache) { cache[key]?.let { return@withContext it } }

        val result = extract(context, track.uri, track.durationMs, bars)
            ?: return@withContext null

        synchronized(cache) {
            if (cache.size >= MAX_CACHE) cache.remove(cache.keys.first())
            cache[key] = result
        }
        result
    }

    /**
     * Decodifica el archivo completo vía MediaCodec y calcula RMS por barra.
     * No guarda todas las muestras en RAM: acumula sumSq por barra a medida que decodifica.
     */
    private fun extract(context: Context, uri: Uri, durationMs: Long, bars: Int): List<Float>? {
        if (bars <= 0) return null
        val extractor = MediaExtractor()
        var codec: MediaCodec? = null
        return try {
            extractor.setDataSource(context, uri, null)
            val trackIndex = (0 until extractor.trackCount).firstOrNull { i ->
                extractor.getTrackFormat(i).getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true
            } ?: return null

            extractor.selectTrack(trackIndex)
            val format = extractor.getTrackFormat(trackIndex)
            val mime = format.getString(MediaFormat.KEY_MIME) ?: return null
            val sampleRate = try { format.getInteger(MediaFormat.KEY_SAMPLE_RATE) } catch (_: Exception) { 44100 }
            val channelCount = try { format.getInteger(MediaFormat.KEY_CHANNEL_COUNT) } catch (_: Exception) { 2 }
            val estimatedTotalFrames: Long = if (durationMs > 0 && sampleRate > 0) {
                durationMs * sampleRate / 1000
            } else {
                // fallback: si no hay duración, asumimos 3 minutos
                180L * sampleRate
            }
            val samplesPerBar = (estimatedTotalFrames / bars).coerceAtLeast(1024L)

            // Acumuladores por barra
            val sumSqPerBar = DoubleArray(bars)
            val countPerBar = LongArray(bars)
            var currentBar = 0
            var samplesInCurrentBar = 0L
            var globalMaxRms = 1e-9

            codec = MediaCodec.createDecoderByType(mime)
            codec.configure(format, null, null, 0)
            codec.start()

            val info = MediaCodec.BufferInfo()
            var inputDone = false
            var outputDone = false
            var pcmEncodingBits = 16
            var deadlines = 0
            val deadlineNanos = System.nanoTime() + 4_000_000_000L // 4s máx por archivo

            while (!outputDone && System.nanoTime() < deadlineNanos) {
                if (!inputDone) {
                    val inIndex = codec.dequeueInputBuffer(TIMEOUT_US)
                    if (inIndex >= 0) {
                        val buf = codec.getInputBuffer(inIndex) ?: break
                        val size = extractor.readSampleData(buf, 0)
                        if (size < 0) {
                            codec.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            inputDone = true
                        } else {
                            codec.queueInputBuffer(inIndex, 0, size, extractor.sampleTime, 0)
                            extractor.advance()
                        }
                    }
                }

                val outIndex = codec.dequeueOutputBuffer(info, TIMEOUT_US)
                when {
                    outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        val outFormat = codec.outputFormat
                        if (outFormat.containsKey(MediaFormat.KEY_PCM_ENCODING)) {
                            pcmEncodingBits = when (outFormat.getInteger(MediaFormat.KEY_PCM_ENCODING)) {
                                AudioFormat.ENCODING_PCM_FLOAT -> 32
                                AudioFormat.ENCODING_PCM_24BIT_PACKED -> 24
                                AudioFormat.ENCODING_PCM_32BIT -> 32
                                else -> 16
                            }
                        }
                    }
                    outIndex >= 0 -> {
                        val outBuf = codec.getOutputBuffer(outIndex)
                        if (outBuf != null && info.size > 0) {
                            outBuf.order(ByteOrder.nativeOrder())
                            outBuf.position(info.offset)
                            outBuf.limit(info.offset + info.size)

                            // Decodifica y acumula RMS directamente, sin guardar todo en lista
                            when (pcmEncodingBits) {
                                32 -> {
                                    // Float PCM: 4 bytes por muestra
                                    while (outBuf.remaining() >= 4 * channelCount) {
                                        var monoSum = 0f
                                        repeat(channelCount) {
                                            if (outBuf.remaining() >= 4) monoSum += outBuf.float
                                        }
                                        val mono = monoSum / channelCount
                                        accumulate(mono, sumSqPerBar, countPerBar, samplesPerBar, bars, currentBarRef = { currentBar }, advance = { if (++samplesInCurrentBar >= samplesPerBar && currentBar < bars - 1) { currentBar++; samplesInCurrentBar = 0 } })
                                    }
                                }
                                24 -> {
                                    while (outBuf.remaining() >= 3 * channelCount) {
                                        var monoSum = 0f
                                        repeat(channelCount) {
                                            if (outBuf.remaining() >= 3) {
                                                val b0 = outBuf.get().toInt() and 0xFF
                                                val b1 = outBuf.get().toInt() and 0xFF
                                                val b2 = outBuf.get().toInt()
                                                var v = (b2 shl 16) or (b1 shl 8) or b0
                                                if (v and 0x800000 != 0) v = v or -0x1000000
                                                monoSum += v / 8388608f
                                            }
                                        }
                                        val mono = monoSum / channelCount
                                        accumulate(mono, sumSqPerBar, countPerBar, samplesPerBar, bars, currentBarRef = { currentBar }, advance = { if (++samplesInCurrentBar >= samplesPerBar && currentBar < bars - 1) { currentBar++; samplesInCurrentBar = 0 } })
                                    }
                                }
                                else -> { // 16-bit
                                    while (outBuf.remaining() >= 2 * channelCount) {
                                        var monoSum = 0f
                                        repeat(channelCount) {
                                            if (outBuf.remaining() >= 2) monoSum += outBuf.short / 32768f
                                        }
                                        val mono = monoSum / channelCount
                                        accumulate(mono, sumSqPerBar, countPerBar, samplesPerBar, bars, currentBarRef = { currentBar }, advance = { if (++samplesInCurrentBar >= samplesPerBar && currentBar < bars - 1) { currentBar++; samplesInCurrentBar = 0 } })
                                    }
                                }
                            }
                        }
                        if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) outputDone = true
                        codec.releaseOutputBuffer(outIndex, false)
                    }
                    outIndex == MediaCodec.INFO_TRY_AGAIN_LATER -> {
                        if (++deadlines > 2000) outputDone = true
                    }
                }
            }

            // Calcula RMS por barra
            val rmsPerBar = DoubleArray(bars) { i ->
                val c = countPerBar[i]
                if (c == 0L) 0.0 else sqrt(sumSqPerBar[i] / c)
            }
            val maxRms = rmsPerBar.maxOrNull()?.takeIf { it > 1e-9 } ?: 1.0

            // Normaliza 0..1 con compresión visual (pow 0.58) para que se vea como en GIF
            // Silencio real => RMS ~0 => barra plana (0.06 altura mínima)
            val normalized = rmsPerBar.map { rms ->
                val n = (rms / maxRms).coerceIn(0.0, 1.0)
                // Si el track es silencioso, maxRms será muy pequeño y n será ~1 aunque sea silencio?
                // Para detectar silencio, miramos maxRms absoluto: si es < 0.005 (~ -46dB) es silencio
                if (maxRms < 0.005) 0.06 else n.pow(0.58)
            }.map { it.toFloat().coerceIn(0f, 1f) }

            // Si más de 70% de barras con energía < 0.08, es canción silenciosa -> asegura planicie
            val silentBars = normalized.count { it < 0.08f }
            if (silentBars > bars * 0.7) {
                // mantiene pero aplana más: ya es correcto
            }

            normalized
        } catch (_: Exception) {
            null
        } finally {
            try { codec?.stop(); codec?.release() } catch (_: Exception) {}
            try { extractor.release() } catch (_: Exception) {}
        }
    }

    private inline fun accumulate(
        mono: Float,
        sumSq: DoubleArray,
        count: LongArray,
        samplesPerBar: Long,
        bars: Int,
        currentBarRef: () -> Int,
        advance: () -> Unit
    ) {
        val bar = currentBarRef()
        if (bar >= bars) return
        sumSq[bar] += mono.toDouble() * mono.toDouble()
        count[bar]++
        advance()
    }
}
