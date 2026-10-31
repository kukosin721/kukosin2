package com.kukosin.app.playback

import android.content.Context
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF
import androidx.media3.exoplayer.DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON
import androidx.media3.exoplayer.DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink
import com.kukosin.app.audio.OboeAudioManager
import com.kukosin.app.audio.OboeAudioOutputProvider
import io.github.anilbeesetti.nextlib.media3ext.ffdecoder.NextRenderersFactory

@UnstableApi
class HiFiRenderersFactory(context: Context) : NextRenderersFactory(context) {
    init {
        val config = AudioSettingsManager.configState.value
        val extensionMode = when (config.decoder) {
            "MediaCodec" -> EXTENSION_RENDERER_MODE_OFF
            "Automático" -> EXTENSION_RENDERER_MODE_ON
            else -> EXTENSION_RENDERER_MODE_PREFER
        }
        setExtensionRendererMode(extensionMode)
        // Integer PCM stays integer when bit-perfect. Float output would
        // requantize 16/24-bit files through IEEE 754.
        setEnableAudioFloatOutput(!OboeAudioManager.isBitPerfect(config))
        setEnableAudioTrackPlaybackParams(false)
    }

    override fun buildAudioSink(
        context: Context,
        enableFloatOutput: Boolean,
        enableAudioTrackPlaybackParams: Boolean,
    ): AudioSink {
        val config = AudioSettingsManager.configState.value
        val bitPerfect = OboeAudioManager.isBitPerfect(config)
        val processors = ArrayList<AudioProcessor>()

        if (!bitPerfect && config.targetSampleRateHz > 0) {
            processors.add(SampleRateAudioProcessor(config.targetSampleRateHz))
        }

        val enableFloat = !bitPerfect && when (config.targetPcmFormat) {
            "PCM Int 16-bit", "PCM Int 24-bit", "Auto" -> false
            else -> true
        }

        if (OboeAudioManager.usesOboeEngine(config)) {
            return DefaultAudioSink.Builder(context)
                .setEnableFloatOutput(enableFloat)
                .setAudioOutputProvider(OboeAudioOutputProvider())
                .build()
        }

        return DefaultAudioSink.Builder(context)
            .setEnableFloatOutput(enableFloat)
            .setEnableAudioTrackPlaybackParams(false)
            .setAudioProcessors(processors.toTypedArray())
            .build()
    }
}
