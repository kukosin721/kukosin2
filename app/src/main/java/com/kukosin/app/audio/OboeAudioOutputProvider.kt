package com.kukosin.app.audio

import android.media.AudioFormat
import androidx.media3.common.Format
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.audio.AudioOutput
import androidx.media3.exoplayer.audio.AudioOutputProvider
import com.kukosin.app.playback.AudioRoute
import com.kukosin.app.playback.AudioSettingsManager
import com.kukosin.app.playback.BitPerfectMixer
import java.util.concurrent.CopyOnWriteArraySet

/**
 * Creates [OboeAudioOutput] instances for the media3 pipeline.
 *
 * In bit-perfect mode the advertised encoding and sample rate match the
 * decoded source so DefaultAudioSink does not resample or requantize.
 */
@UnstableApi
class OboeAudioOutputProvider : AudioOutputProvider {

    private val listeners = CopyOnWriteArraySet<AudioOutputProvider.Listener>()

    override fun getFormatSupport(
        formatConfig: AudioOutputProvider.FormatConfig,
    ): AudioOutputProvider.FormatSupport {
        val format = formatConfig.format
        return if (format.pcmEncoding != Format.NO_VALUE) {
            AudioOutputProvider.FormatSupport.Builder()
                .setFormatSupportLevel(AudioOutputProvider.FORMAT_SUPPORTED_DIRECTLY)
                .build()
        } else {
            AudioOutputProvider.FormatSupport.UNSUPPORTED
        }
    }

    override fun getOutputConfig(
        formatConfig: AudioOutputProvider.FormatConfig,
    ): AudioOutputProvider.OutputConfig {
        val settings = AudioSettingsManager.configState.value
        val sourceRate = formatConfig.format.sampleRate
        val bitPerfect = OboeAudioManager.isBitPerfect(settings)
        val targetRate = if (bitPerfect || settings.targetSampleRateHz <= 0) {
            sourceRate
        } else {
            settings.targetSampleRateHz
        }

        val channelCount = formatConfig.format.channelCount
        val channelMask = when {
            channelCount == 1 -> AudioFormat.CHANNEL_OUT_MONO
            else -> AudioFormat.CHANNEL_OUT_STEREO
        }

        val encoding = OboeAudioManager.passthroughEncoding(settings, formatConfig.format.pcmEncoding)
        return AudioOutputProvider.OutputConfig.Builder()
            .setEncoding(encoding)
            .setSampleRate(targetRate)
            .setChannelMask(channelMask)
            .build()
    }

    override fun getAudioOutput(config: AudioOutputProvider.OutputConfig): AudioOutput {
        val settings = AudioSettingsManager.configState.value
        val output = AudioRoute.current.value
        val channelCount = Integer.bitCount(config.channelMask).coerceAtLeast(1)
        val pcm = OboeAudioManager.oboeFormatForEncoding(config.encoding)
        val engineConfig = OboeAudioManager.toEngineConfig(
            settings,
            sampleRateHz = config.sampleRate,
            channelCount = channelCount,
            pcmFormat = pcm,
            output = output,
        )

        BitPerfectMixer.apply(output, config.sampleRate, config.encoding, config.channelMask)

        val engine = AudioEngine()
        engine.setSourceMode(SourceMode.PCM)
        val result = engine.configure(engineConfig)
        if (result.code != NativeResult.OK.code) {
            engine.close()
            throw AudioOutputProvider.InitializationException(
                IllegalStateException("Oboe open failed: $result"),
            )
        }
        return OboeAudioOutput(config, engine)
    }

    override fun addListener(listener: AudioOutputProvider.Listener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: AudioOutputProvider.Listener) {
        listeners.remove(listener)
    }

    override fun release() {
        listeners.clear()
    }
}
