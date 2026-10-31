package com.kukosin.app.audio

import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import com.kukosin.app.playback.AudioConfig
import com.kukosin.app.playback.AudioRoute
import com.kukosin.app.playback.OutputDevice

/**
 * Bridges the UI audio settings ([AudioConfig]) to the native engine
 * configuration. This is the single place that maps the panel strings to the
 * stable JNI protocol codes defined in [AudioDriver] / [PcmFormat].
 */
@UnstableApi
object OboeAudioManager {

    fun driverFor(value: String, forceAAudio: Boolean = false): AudioDriver {
        if (forceAAudio) return AudioDriver.AAUDIO
        return when (value) {
            "AAudio" -> AudioDriver.AAUDIO
            "OpenSL ES" -> AudioDriver.OPENSL_ES
            "Hi-Res Direct" -> AudioDriver.AAUDIO
            else -> AudioDriver.AAUDIO
        }
    }

    fun isBitPerfect(config: AudioConfig): Boolean =
        config.bitPerfectEnabled || config.driver == "Hi-Res Direct"

    fun usesOboeEngine(config: AudioConfig): Boolean {
        val output = AudioRoute.current.value
        if (output.isUsb && isBitPerfect(config)) return true
        if (config.driver == "AudioTrack") return config.useOboeDirectOutput
        return true
    }

    fun outputPathFor(config: AudioConfig, output: OutputDevice): OutputPath = when {
        output.isUsb -> OutputPath.USB_DAC
        config.driver == "Hi-Res Direct" -> OutputPath.DIRECT
        output.isBuiltin -> OutputPath.INTEGRATED_EXCLUSIVE
        else -> OutputPath.DIRECT
    }

    fun formatFor(value: String): PcmFormat = when (value) {
        "PCM Int 16-bit" -> PcmFormat.INT16
        "PCM Int 24-bit" -> PcmFormat.INT24
        "PCM Int 32-bit" -> PcmFormat.INT32
        "PCM Float 32-bit" -> PcmFormat.FLOAT32
        else -> PcmFormat.INT16
    }

    fun pcmFormatFromSource(pcmEncoding: Int): PcmFormat = when (pcmEncoding) {
        C.ENCODING_PCM_16BIT -> PcmFormat.INT16
        C.ENCODING_PCM_24BIT -> PcmFormat.INT24
        C.ENCODING_PCM_32BIT -> PcmFormat.INT32
        C.ENCODING_PCM_FLOAT -> PcmFormat.FLOAT32
        else -> PcmFormat.INT16
    }

    fun media3Encoding(format: PcmFormat): Int = when (format) {
        PcmFormat.INT16 -> C.ENCODING_PCM_16BIT
        PcmFormat.INT24 -> C.ENCODING_PCM_24BIT
        PcmFormat.INT32 -> C.ENCODING_PCM_32BIT
        PcmFormat.FLOAT32 -> C.ENCODING_PCM_FLOAT
    }

    /** Keep the decoder's PCM encoding so DefaultAudioSink does not requantize. */
    fun passthroughEncoding(config: AudioConfig, sourceEncoding: Int): Int {
        if (isBitPerfect(config) || config.targetPcmFormat == "Auto") {
            return if (sourceEncoding != androidx.media3.common.Format.NO_VALUE) {
                sourceEncoding
            } else {
                C.ENCODING_PCM_16BIT
            }
        }
        return media3Encoding(formatFor(config.targetPcmFormat))
    }

    fun oboeFormatForEncoding(encoding: Int): PcmFormat = when (encoding) {
        C.ENCODING_PCM_16BIT -> PcmFormat.INT16
        C.ENCODING_PCM_24BIT -> PcmFormat.INT24
        C.ENCODING_PCM_32BIT -> PcmFormat.INT32
        C.ENCODING_PCM_FLOAT -> PcmFormat.FLOAT32
        else -> PcmFormat.INT16
    }

    fun toEngineConfig(
        config: AudioConfig,
        sampleRateHz: Int,
        channelCount: Int,
        pcmFormat: PcmFormat = formatFor(config.targetPcmFormat),
        output: OutputDevice = AudioRoute.current.value,
    ): AudioEngineConfig {
        val bitPerfect = isBitPerfect(config)
        val path = outputPathFor(config, output)
        val usb = path == OutputPath.USB_DAC
        return AudioEngineConfig(
            driver = driverFor(config.driver, forceAAudio = usb || bitPerfect),
            format = pcmFormat,
            sampleRateHz = sampleRateHz,
            channelCount = channelCount,
            sharingMode = if (bitPerfect || usb) SharingMode.EXCLUSIVE else SharingMode.SHARED,
            performanceMode = if (bitPerfect || usb) PerformanceMode.NONE else PerformanceMode.LOW_LATENCY,
            deviceId = output.audioDeviceId,
            bitPerfect = bitPerfect || usb,
            outputPath = path,
        )
    }
}
