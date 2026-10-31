package com.kukosin.app.data.analysis

import android.content.Context
import android.net.Uri
import com.kukosin.app.data.Track
import com.kukosin.app.data.bitDepthFromSampleFormat
import io.github.anilbeesetti.nextlib.mediainfo.MediaInfoBuilder
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

enum class AuthenticityVerdict {
    AUTHENTIC_HIRES,
    AUTHENTIC_CD,
    AUTHENTIC_LOSSY,
    UPSAMPLED,
    LOSSY_TRANSCODE,
    INCONCLUSIVE,
}

data class AudioAnalysis(
    val uri: Uri,
    val isAnalyzing: Boolean = false,
    val error: String? = null,
    val lyrics: EmbeddedLyrics? = null,
    val tags: List<Pair<String, String>> = emptyList(),
    val spectrumDb: List<Float> = emptyList(),
    val spectrumHz: List<Float> = emptyList(),
    val cutoffHz: Int = 0,
    val nyquistHz: Int = 0,
    val peakDb: Float = 0f,
    val rmsDb: Float = 0f,
    val dynamicRangeDb: Float = 0f,
    val hfEnergyRatio: Float = 0f,
    val lsbPaddingRatio: Float? = null,
    val verdict: AuthenticityVerdict = AuthenticityVerdict.INCONCLUSIVE,
    val verdictTitle: String = "",
    val verdictDetail: String = "",
    val technical: List<Pair<String, String>> = emptyList(),
)

object AudioAnalyzer {
    fun extractMetadataOnly(context: Context, track: Track): AudioAnalysis {
        val tags = LinkedHashMap<String, String>()
        val snapshot = runCatching { EmbeddedMetadata.extract(context, track.uri, track.filePath) }.getOrNull()
        snapshot?.fields?.forEach { tags[it.first] = it.second }

        val mediaInfo = runCatching { MediaInfoBuilder().from(context, track.uri).build() }.getOrNull()
        val audio = mediaInfo?.audioStreams?.firstOrNull()
        if (mediaInfo != null) {
            tags["Contenedor"] = mediaInfo.format.ifBlank { track.mimeType }
            tags["Duración FFmpeg"] = "${mediaInfo.duration} ms"
            if (mediaInfo.chapters.isNotEmpty()) tags["Capítulos"] = mediaInfo.chapters.size.toString()
        }
        if (audio != null) {
            tags["Codec FFmpeg"] = audio.codecName
            tags["Sample format FFmpeg"] = audio.sampleFormat.orEmpty()
            tags["Sample rate FFmpeg"] = "${audio.sampleRate} Hz"
            tags["Canales FFmpeg"] = audio.channels.toString()
            if (audio.bitRate > 0) tags["Bitrate FFmpeg"] = "${audio.bitRate / 1000} kbps (${audio.bitRate} bps)"
        }

        val sampleRate = when {
            audio != null && audio.sampleRate > 0 -> audio.sampleRate
            track.sampleRate > 0 -> track.sampleRate
            else -> 0
        }
        val bitDepth = bitDepthFromSampleFormat(audio?.sampleFormat).takeIf { it > 0 } ?: track.bitDepth
        val channels = (audio?.channels ?: track.channels).coerceAtLeast(0)
        val bitrate = (audio?.bitRate?.toInt()?.takeIf { it > 0 } ?: track.bitrate)
        val losslessUncompressed = if (sampleRate > 0 && bitDepth > 0 && channels > 0) {
            sampleRate.toLong() * bitDepth * channels
        } else 0L
        if (losslessUncompressed > 0) {
            tags["Tasa uncompressed (PCM)"] = "${losslessUncompressed / 1000} kbps"
            if (bitrate > 0) {
                val ratio = losslessUncompressed.toDouble() / bitrate
                val compressionPct = ((1.0 - (1.0 / ratio)) * 100.0).coerceIn(0.0, 99.0)
                tags["Ratio de compresión"] = "%.2f:1 (%.1f%% comprimido)".format(ratio, compressionPct)
            }
        }
        tags["Tamaño del archivo"] = track.fileSizeLabel
        tags["Ruta completa"] = track.filePath.ifBlank { track.uri.toString() }
        tags["Hi-Res Declarado"] = if (sampleRate >= 88200 || bitDepth > 16) "Sí" else "No"

        return AudioAnalysis(
            uri = track.uri,
            lyrics = snapshot?.lyrics,
            tags = tags.toList(),
            nyquistHz = if (sampleRate > 0) sampleRate / 2 else 0,
            verdict = AuthenticityVerdict.INCONCLUSIVE,
            verdictTitle = "Verificación de autenticidad pendiente",
            verdictDetail = "Presiona 'Verificar Autenticidad y Espectro' para analizar muestras PCM, medir la frecuencia de corte real y validar si es un archivo Hi-Res genuino o un transcode.",
            technical = tags.toList(),
        )
    }

    suspend fun analyze(context: Context, track: Track): AudioAnalysis {
        val tags = LinkedHashMap<String, String>()
        val snapshot = runCatching { EmbeddedMetadata.extract(context, track.uri, track.filePath) }.getOrNull()
        snapshot?.fields?.forEach { tags[it.first] = it.second }

        val mediaInfo = runCatching { MediaInfoBuilder().from(context, track.uri).build() }.getOrNull()
        val audio = mediaInfo?.audioStreams?.firstOrNull()
        if (mediaInfo != null) {
            tags["Contenedor"] = mediaInfo.format.ifBlank { track.mimeType }
            tags["Duración FFmpeg"] = "${mediaInfo.duration} ms"
            if (mediaInfo.chapters.isNotEmpty()) tags["Capítulos"] = mediaInfo.chapters.size.toString()
        }
        if (audio != null) {
            tags["Codec FFmpeg"] = audio.codecName
            tags["Sample format FFmpeg"] = audio.sampleFormat.orEmpty()
            tags["Sample rate FFmpeg"] = "${audio.sampleRate} Hz"
            tags["Canales FFmpeg"] = audio.channels.toString()
            if (audio.bitRate > 0) tags["Bitrate FFmpeg"] = "${audio.bitRate / 1000} kbps (${audio.bitRate} bps)"
            if (!audio.language.isNullOrBlank()) tags["Idioma pista"] = audio.language.orEmpty()
            if (!audio.title.isNullOrBlank()) tags["Título de stream"] = audio.title.orEmpty()
        }

        val sampleRate = when {
            audio != null && audio.sampleRate > 0 -> audio.sampleRate
            track.sampleRate > 0 -> track.sampleRate
            else -> 0
        }
        val bitDepth = bitDepthFromSampleFormat(audio?.sampleFormat).takeIf { it > 0 } ?: track.bitDepth
        val channels = (audio?.channels ?: track.channels).coerceAtLeast(0)
        val bitrate = (audio?.bitRate?.toInt()?.takeIf { it > 0 } ?: track.bitrate)
        val losslessUncompressed = if (sampleRate > 0 && bitDepth > 0 && channels > 0) {
            sampleRate.toLong() * bitDepth * channels
        } else 0L
        if (losslessUncompressed > 0) {
            tags["Tasa uncompressed (PCM)"] = "${losslessUncompressed / 1000} kbps"
            if (bitrate > 0) {
                val ratio = losslessUncompressed.toDouble() / bitrate
                val compressionPct = ((1.0 - (1.0 / ratio)) * 100.0).coerceIn(0.0, 99.0)
                tags["Ratio de compresión"] = "%.2f:1 (%.1f%% comprimido)".format(ratio, compressionPct)
            }
        }
        tags["Tamaño del archivo"] = track.fileSizeLabel
        tags["Ruta completa"] = track.filePath.ifBlank { track.uri.toString() }
        tags["Hi-Res Declarado"] = if (sampleRate >= 88200 || bitDepth > 16) "Sí" else "No"

        val capture = runCatching { PcmDecoder.capture(context, track.uri, track.durationMs) }.getOrNull()
        if (capture == null) {
            return AudioAnalysis(
                uri = track.uri,
                lyrics = snapshot?.lyrics,
                tags = tags.toList(),
                nyquistHz = if (sampleRate > 0) sampleRate / 2 else 0,
                verdict = AuthenticityVerdict.INCONCLUSIVE,
                verdictTitle = "Sin señal PCM capturada",
                verdictDetail = "Se leyeron metadatos del archivo, pero el decodificador no entregó muestras PCM para el análisis espectral.",
                technical = tags.toList(),
            )
        }

        val usedRate = capture.sampleRate.takeIf { it > 0 } ?: sampleRate
        val nyquist = usedRate / 2
        val (spectrumDb, cutoff, slope, hfRatio, peakDb, rmsDb) = measureSpectrum(capture, usedRate)
        val bars = downsample(spectrumDb, 96, usedRate)
        val dr = (peakDb - rmsDb).coerceAtLeast(0f)
        val padding = capture.lsbPaddingRatio
        val (verdict, title, detail) = classify(
            codec = track.codecLabel,
            sampleRate = usedRate,
            bitDepth = bitDepth,
            cutoffHz = cutoff,
            slope = slope,
            nyquistHz = nyquist,
            hfRatio = hfRatio,
            padding = padding,
            bitrate = bitrate,
        )

        val drRating = when {
            dr >= 14f -> "Excelente (DR${dr.roundToInt()})"
            dr >= 10f -> "Bueno (DR${dr.roundToInt()})"
            dr >= 7f -> "Comprimido (DR${dr.roundToInt()})"
            else -> "Loudness War / Muy comprimido (DR${dr.roundToInt()})"
        }

        val technical = buildList {
            add("Veredicto final" to title)
            add("Diagnóstico técnico" to detail)
            add("Tasa de muestreo (PCM)" to "$usedRate Hz")
            add("Frecuencia Nyquist" to "$nyquist Hz")
            add("Corte espectral medido" to "$cutoff Hz (${"%.1f".format(cutoff / 1000.0)} kHz)")
            add("Pendiente de caída (Roll-off)" to "%.1f dB/kHz (${if (slope > 20f) "Filtro Brickwall Lossy" else "Caída acústica suave"})".format(slope))
            add("Uso de ancho de banda" to "${"%.1f".format((cutoff.toFloat() / nyquist.coerceAtLeast(1)) * 100)} % de Nyquist")
            add("Energía en armónicos >20 kHz" to if (hfRatio > 0.0001f) "%.4f %% (Armónicos ultrasónicos activos)".format(hfRatio * 100) else "Inexistente / Filtrada")
            add("Volumen True Peak" to "%.1f dBFS".format(peakDb))
            add("Volumen RMS" to "%.1f dBFS".format(rmsDb))
            add("Rango Dinámico (DR)" to "%.1f dB — $drRating".format(dr))
            add("Bits de decodificación PCM" to "${capture.pcmEncodingBits}-bit")
            if (padding != null && bitDepth >= 24) {
                val paddingPct = padding * 100f
                val paddedText = if (paddingPct > 88f) "FALSO 24-BIT (%.1f%% LSBs en cero)".format(paddingPct) else "24-bit real (%.1f%% LSBs en cero)".format(paddingPct)
                add("Verificación LSB 24-bit" to paddedText)
            }
            add("Muestreo multi-punto" to "${capture.monoWindows.size} ventanas FFT de 4096 muestras")
            addAll(tags.toList())
        }

        return AudioAnalysis(
            uri = track.uri,
            lyrics = snapshot?.lyrics,
            tags = tags.toList(),
            spectrumDb = bars.first,
            spectrumHz = bars.second,
            cutoffHz = cutoff,
            nyquistHz = nyquist,
            peakDb = peakDb,
            rmsDb = rmsDb,
            dynamicRangeDb = dr,
            hfEnergyRatio = hfRatio,
            lsbPaddingRatio = padding,
            verdict = verdict,
            verdictTitle = title,
            verdictDetail = detail,
            technical = technical,
        )
    }

    private fun measureSpectrum(
        capture: PcmCapture,
        sampleRate: Int,
    ): PreciseStats {
        val n = 4096
        val mag = DoubleArray(n / 2)
        var peak = 1e-9
        var sumSq = 0.0
        var count = 0
        capture.monoWindows.forEach { window ->
            if (window.size < n) return@forEach
            val real = DoubleArray(n)
            val imag = DoubleArray(n)
            for (i in 0 until n) {
                val hann = 0.5 * (1.0 - cos(2.0 * PI * i / (n - 1)))
                val sample = window[i].toDouble()
                real[i] = sample * hann
                peak = max(peak, abs(sample))
                sumSq += sample * sample
                count++
            }
            Fft.transform(real, imag)
            for (i in mag.indices) {
                mag[i] += sqrt(real[i] * real[i] + imag[i] * imag[i])
            }
        }
        val frames = capture.monoWindows.size.coerceAtLeast(1)
        val db = List(mag.size) { index ->
            val value = (mag[index] / frames).coerceAtLeast(1e-12)
            (20.0 * log10(value)).toFloat()
        }
        val binHz = sampleRate.toFloat() / n
        val (cutoffBin, cutoff) = detectCutoffWithBin(db, binHz, sampleRate / 2)
        val slope = calculateRollOffSlope(db, binHz, cutoffBin)

        val hz20 = (20000f / binHz).toInt().coerceIn(0, db.lastIndex)
        val energyAll = db.sumOf { 10.0.pow(it / 10.0) }
        val energyHf = db.drop(hz20).sumOf { 10.0.pow(it / 10.0) }
        val rms = if (count > 0) sqrt(sumSq / count) else 1e-9
        val peakDb = (20.0 * log10(peak.coerceAtLeast(1e-9))).toFloat()
        val rmsDb = (20.0 * log10(rms.coerceAtLeast(1e-9))).toFloat()
        return PreciseStats(db.toList(), cutoff, slope, (energyHf / energyAll.coerceAtLeast(1e-12)).toFloat(), peakDb, rmsDb)
    }

    private fun detectCutoffWithBin(db: List<Float>, binHz: Float, nyquist: Int): Pair<Int, Int> {
        if (db.isEmpty() || binHz <= 0f) return 0 to 0
        val from = (2000f / binHz).toInt().coerceIn(0, db.lastIndex)
        val to = (10000f / binHz).toInt().coerceIn(from + 1, db.lastIndex)
        val floorRef = if (to > from) db.subList(from, to).average().toFloat() else -40f
        val threshold = (floorRef - 34f).coerceAtMost(-68f)
        var lastAliveBin = from
        val windowBins = (400f / binHz).toInt().coerceAtLeast(4)
        var i = from
        while (i < db.size) {
            val hz = i * binHz
            if (hz >= nyquist) break
            val end = (i + windowBins).coerceAtMost(db.size)
            val avg = db.subList(i, end).average().toFloat()
            if (avg > threshold) {
                lastAliveBin = i + windowBins / 2
            }
            i += windowBins / 2
        }
        val cutoffHz = (lastAliveBin * binHz).toInt().coerceIn(0, nyquist)
        return lastAliveBin to cutoffHz
    }

    private fun calculateRollOffSlope(db: List<Float>, binHz: Float, cutoffBin: Int): Float {
        if (cutoffBin <= 0 || cutoffBin >= db.size - 4 || binHz <= 0f) return 0f
        val spanBins = (1500f / binHz).toInt().coerceAtLeast(4)
        val endBin = (cutoffBin + spanBins).coerceAtMost(db.lastIndex)
        val dropDb = db[cutoffBin] - db[endBin]
        val distanceKhz = (endBin - cutoffBin) * binHz / 1000f
        return if (distanceKhz > 0f) (dropDb / distanceKhz).coerceAtLeast(0f) else 0f
    }

    private fun downsample(db: List<Float>, bars: Int, sampleRate: Int): Pair<List<Float>, List<Float>> {
        if (db.isEmpty()) return emptyList<Float>() to emptyList()
        val nyquist = sampleRate / 2f
        val chunk = db.size / bars.coerceAtLeast(1)
        val values = ArrayList<Float>(bars)
        val hz = ArrayList<Float>(bars)
        for (i in 0 until bars) {
            val start = i * chunk
            val end = (start + chunk).coerceAtMost(db.size)
            if (start >= end) break
            values += db.subList(start, end).max()
            hz += (start + end) / 2f / db.size * nyquist
        }
        return values to hz
    }

    private fun classify(
        codec: String,
        sampleRate: Int,
        bitDepth: Int,
        cutoffHz: Int,
        slope: Float,
        nyquistHz: Int,
        hfRatio: Float,
        padding: Float?,
        bitrate: Int,
    ): Triple<AuthenticityVerdict, String, String> {
        val lossyCodec = codec in listOf("MP3", "AAC", "OGG", "Opus", "AC3", "E-AC3")
        val padded = padding != null && padding > 0.88f && bitDepth >= 24

        return when {
            padded -> Triple(
                AuthenticityVerdict.UPSAMPLED,
                "Falso 24-bit (Relleno de ceros LSB)",
                "El ${(padding * 100).roundToInt()}% de los bits menos significativos (LSB) son ceros. Es un archivo de 16-bit empaquetado dentro de un contenedor de $bitDepth-bit.",
            )
            !lossyCodec && cutoffHz < 19_800 && slope > 18f -> Triple(
                AuthenticityVerdict.LOSSY_TRANSCODE,
                "Falso Lossless (Transcode MP3/AAC → $codec)",
                "Declara $codec $bitDepth-bit, pero tiene un corte tajante a ${cutoffHz} Hz (${"%.1f".format(cutoffHz / 1000.0)} kHz) con pendiente brickwall de ${"%.1f".format(slope)} dB/kHz, firma inconfundible de audio comprimido con pérdida re-codificado.",
            )
            sampleRate >= 88_200 && cutoffHz < 24_000 -> Triple(
                AuthenticityVerdict.UPSAMPLED,
                "Falso Hi-Res (Upsampled / Resampled)",
                "Declara muestreo de ${sampleRate / 1000} kHz (Nyquist ${nyquistHz / 1000} kHz), pero la señal cae en ${cutoffHz} Hz (${"%.1f".format(cutoffHz / 1000.0)} kHz). Es material de calidad CD/48k re-muestreado a alta resolución.",
            )
            sampleRate >= 88_200 && (cutoffHz >= 28_000 || hfRatio > 0.0003f) -> Triple(
                AuthenticityVerdict.AUTHENTIC_HIRES,
                "Hi-Res 100% Auténtico",
                "Se confirma espectro analógico continuo con armónicos ultrasónicos activos hasta ${cutoffHz} Hz (${"%.1f".format(cutoffHz / 1000.0)} kHz) y profundidad real de bits.",
            )
            sampleRate in 44_000..48_100 && cutoffHz >= 19_000 -> Triple(
                AuthenticityVerdict.AUTHENTIC_CD,
                "Calidad CD / Lossless Auténtico",
                "El ancho de banda alcanza ${cutoffHz} Hz, acorde al límite Nyquist de $nyquistHz Hz para audio sin pérdida de 44.1/48 kHz.",
            )
            lossyCodec -> Triple(
                AuthenticityVerdict.AUTHENTIC_LOSSY,
                "Audio Compreso Auténtico ($codec)",
                "Corte espectral en ${cutoffHz} Hz con pendiente típica de $codec a ${if (bitrate > 0) "${bitrate / 1000} kbps" else "tasa variable"}.",
            )
            else -> Triple(
                AuthenticityVerdict.INCONCLUSIVE,
                "Análisis Espectral Inconcluso",
                "Corte espectral en ${cutoffHz} Hz frente a Nyquist de $nyquistHz Hz. Puede ser una grabación histórica o master con filtro paso bajo de estudio.",
            )
        }
    }

    private data class PreciseStats(
        val db: List<Float>,
        val cutoff: Int,
        val slope: Float,
        val hfRatio: Float,
        val peakDb: Float,
        val rmsDb: Float,
    )
}
