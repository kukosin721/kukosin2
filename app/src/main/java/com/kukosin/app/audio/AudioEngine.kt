package com.kukosin.app.audio

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer

/**
 * Stable protocol mirrored in native code (native-lib.cpp). Do not reorder
 * the enum codes without updating the JNI mapping.
 */
enum class AudioDriver(val code: Int) {
    AAUDIO(0),
    OPENSL_ES(1),
    AUTO(-1),
}

enum class PcmFormat(val code: Int) {
    INT16(0),
    INT24(1),
    FLOAT32(2),
    INT32(3),
}

enum class SharingMode(val code: Int) {
    SHARED(0),
    EXCLUSIVE(1),
}

enum class PerformanceMode(val code: Int) {
    NONE(0),
    POWER_SAVING(1),
    LOW_LATENCY(2),
}

enum class SourceMode(val code: Int) {
    TONE(0),
    PCM(1),
}

/** Native output path: direct controller, integrated exclusive AAudio, USB DAC. */
enum class OutputPath(val code: Int) {
    DIRECT(0),
    INTEGRATED_EXCLUSIVE(1),
    USB_DAC(2),
}

enum class EngineState(val code: Int) {
    UNINITIALIZED(0),
    UNKNOWN(1),
    OPEN(2),
    STARTING(3),
    STARTED(4),
    PAUSING(5),
    PAUSED(6),
    FLUSHING(7),
    FLUSHED(8),
    STOPPING(9),
    STOPPED(10),
    CLOSING(11),
    CLOSED(12),
    DISCONNECTED(13);

    companion object {
        fun fromCode(code: Int): EngineState = entries.firstOrNull { it.code == code } ?: UNKNOWN
    }
}

/** Mirrors oboe::Result. Unknown/unmatched values fall back to UNKNOWN. */
enum class NativeResult(val code: Int, val description: String) {
    OK(0, "OK"),
    ERROR_DISCONNECTED(-899, "Dispositivo desconectado"),
    ERROR_ILLEGAL_ARGUMENT(-898, "Argumento inválido"),
    ERROR_INVALID_STATE(-895, "Estado inválido"),
    ERROR_UNAVAILABLE(-892, "No disponible"),
    ERROR_NO_MEMORY(-890, "Sin memoria"),
    ERROR_INVALID_FORMAT(-886, "Formato no soportado"),
    ERROR_OUT_OF_RANGE(-885, "Fuera de rango"),
    ERROR_INVALID_RATE(-883, "Sample rate no soportado"),
    ERROR_CLOSED(-882, "Stream cerrado"),
    UNKNOWN(Int.MIN_VALUE, "Error desconocido");

    companion object {
        fun fromCode(code: Int): NativeResult =
            entries.firstOrNull { it.code == code } ?: UNKNOWN
    }
}

data class AudioEngineConfig(
    val driver: AudioDriver = AudioDriver.AAUDIO,
    val format: PcmFormat = PcmFormat.INT16,
    val sampleRateHz: Int = 0,
    val channelCount: Int = 2,
    val sharingMode: SharingMode = SharingMode.EXCLUSIVE,
    val performanceMode: PerformanceMode = PerformanceMode.NONE,
    val deviceId: Int = 0,
    val bitPerfect: Boolean = true,
    val outputPath: OutputPath = OutputPath.DIRECT,
)

/**
 * JNI bridge to the C++ [audio::AudioEngine]. Holds a single native handle
 * and never touches raw audio buffers on the Kotlin side: PCM is forwarded as
 * packed bytes into the engine's ring buffer.
 *
 * All state changes should be called off the main thread (see
 * [configureAndStart] for a coroutine helper).
 */
class AudioEngine : AutoCloseable {

    private var handle: Long = nativeCreate()

    init {
        require(handle != 0L) { "No se pudo crear el motor de audio nativo" }
    }

    val state: EngineState get() = EngineState.fromCode(nativeGetState(handle))

    val actualSampleRate: Int get() = nativeGetActualSampleRate(handle)

    val actualFormat: PcmFormat get() = when (nativeGetActualFormat(handle)) {
        0 -> PcmFormat.INT16
        1 -> PcmFormat.INT24
        3 -> PcmFormat.INT32
        else -> PcmFormat.FLOAT32
    }

    val framesPerBurst: Int get() = nativeGetFramesPerBurst(handle)

    val actualSharingMode: SharingMode
        get() = if (nativeGetSharingMode(handle) == 1) SharingMode.EXCLUSIVE else SharingMode.SHARED

    val actualDeviceId: Int get() = nativeGetActualDeviceId(handle)

    val openedBitPerfect: Boolean get() = nativeGetOpenedBitPerfect(handle)

    /** Total frames rendered by the audio callback, reset on open/flush. */
    val positionFrames: Long get() = nativeGetPositionFrames(handle)

    fun configure(config: AudioEngineConfig): NativeResult = NativeResult.fromCode(
        nativeConfigure(
            handle,
            config.driver.code,
            config.format.code,
            config.sampleRateHz,
            config.channelCount,
            config.sharingMode.code,
            config.performanceMode.code,
            config.deviceId,
            config.bitPerfect,
            config.outputPath.code,
        ),
    )

    fun start(): NativeResult = NativeResult.fromCode(nativeStart(handle))

    fun stop(): NativeResult = NativeResult.fromCode(nativeStop(handle))

    fun pause(): NativeResult = NativeResult.fromCode(nativePause(handle))

    fun flush(): NativeResult = NativeResult.fromCode(nativeFlush(handle))

    fun setVolume(volume: Float) = nativeSetVolume(handle, volume.coerceIn(0f, 1f))

    fun setSourceMode(mode: SourceMode) = nativeSetSourceMode(handle, mode.code)

    /** Pushes interleaved float PCM. Returns the number of samples accepted. */
    fun writePcm(samples: FloatArray): Int = nativeWritePcm(handle, samples, samples.size)

    /**
     * Bit-perfect packed PCM from a direct [ByteBuffer]. Advances nothing;
     * the caller must update [ByteBuffer.position] by the returned byte count.
     */
    fun writePacked(buffer: ByteBuffer): Int {
        val remaining = buffer.remaining()
        if (remaining <= 0) return 0
        if (!buffer.isDirect) {
            val copy = ByteArray(remaining)
            val pos = buffer.position()
            buffer.get(copy)
            buffer.position(pos)
            val tmp = ByteBuffer.allocateDirect(remaining)
            tmp.put(copy)
            return nativeWritePacked(handle, tmp, 0, remaining)
        }
        return nativeWritePacked(handle, buffer, buffer.position(), remaining)
    }

    /** Opens the stream and starts playback on a background dispatcher. */
    suspend fun configureAndStart(config: AudioEngineConfig): NativeResult =
        withContext(Dispatchers.Default) {
            val openResult = configure(config)
            if (openResult.code != NativeResult.OK.code) return@withContext openResult
            start()
        }

    override fun close() {
        if (handle != 0L) {
            nativeDelete(handle)
            handle = 0L
        }
    }

    // --- native interface ---
    private external fun nativeCreate(): Long
    private external fun nativeDelete(handle: Long)
    private external fun nativeConfigure(
        handle: Long,
        api: Int,
        format: Int,
        sampleRate: Int,
        channelCount: Int,
        sharingMode: Int,
        performanceMode: Int,
        deviceId: Int,
        bitPerfect: Boolean,
        outputPath: Int,
    ): Int
    private external fun nativeStart(handle: Long): Int
    private external fun nativeStop(handle: Long): Int
    private external fun nativePause(handle: Long): Int
    private external fun nativeFlush(handle: Long): Int
    private external fun nativeSetVolume(handle: Long, volume: Float)
    private external fun nativeSetSourceMode(handle: Long, mode: Int)
    private external fun nativeWritePcm(handle: Long, samples: FloatArray, sampleCount: Int): Int
    private external fun nativeWritePacked(handle: Long, buffer: ByteBuffer, offset: Int, length: Int): Int
    private external fun nativeGetState(handle: Long): Int
    private external fun nativeGetActualSampleRate(handle: Long): Int
    private external fun nativeGetActualFormat(handle: Long): Int
    private external fun nativeGetFramesPerBurst(handle: Long): Int
    private external fun nativeGetPositionFrames(handle: Long): Long
    private external fun nativeGetSharingMode(handle: Long): Int
    private external fun nativeGetActualDeviceId(handle: Long): Int
    private external fun nativeGetOpenedBitPerfect(handle: Long): Boolean

    companion object {
        init {
            System.loadLibrary("audioengine")
        }
    }
}
