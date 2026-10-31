package com.kukosin.app.audio

import android.media.AudioDeviceInfo
import android.os.SystemClock
import androidx.media3.common.C
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.analytics.PlayerId
import androidx.media3.exoplayer.audio.AudioOutput
import androidx.media3.exoplayer.audio.AudioOutputProvider.OutputConfig
import java.nio.ByteBuffer
import java.util.concurrent.CopyOnWriteArraySet

/**
 * An [AudioOutput] backed by the native Oboe engine. Forwards packed PCM
 * bytes from media3 into the engine ring buffer with no extra conversion.
 */
@UnstableApi
class OboeAudioOutput(
    private val config: OutputConfig,
    private val engine: AudioEngine,
) : AudioOutput {

    private val listeners = CopyOnWriteArraySet<AudioOutput.Listener>()
    private val sampleRate = if (config.sampleRate > 0) config.sampleRate else 48000

    private var playbackParameters = PlaybackParameters.DEFAULT
    private var started = false
    private var stalled = false
    private var released = false

    override fun play() {
        engine.start()
    }

    override fun pause() {
        engine.pause()
    }

    override fun write(
        buffer: ByteBuffer,
        encodedAccessUnitCount: Int,
        presentationTimeUs: Long,
    ): Boolean {
        val remaining = buffer.remaining()
        if (remaining == 0) {
            return true
        }

        val accepted = engine.writePacked(buffer).coerceIn(0, remaining)
        buffer.position(buffer.position() + accepted)
        if (accepted < remaining) {
            stalled = true
            return false
        }

        stalled = false
        if (!started) {
            started = true
            listeners.forEach { it.onPositionAdvancing(SystemClock.elapsedRealtime()) }
        }
        return true
    }

    override fun flush() {
        engine.flush()
        started = false
        stalled = false
    }

    override fun stop() {
        engine.stop()
    }

    override fun release() {
        if (!released) {
            released = true
            engine.close()
            listeners.forEach { it.onReleased() }
        }
    }

    override fun setVolume(volume: Float) {
        // Bit-perfect path ignores digital gain. Hardware/DAC volume stays at the OS mixer.
        if (!engine.openedBitPerfect) {
            engine.setVolume(volume)
        }
    }

    override fun isOffloadedPlayback(): Boolean = false

    override fun getAudioSessionId(): Int = C.AUDIO_SESSION_ID_UNSET

    override fun getSampleRate(): Int = sampleRate

    override fun getBufferSizeInFrames(): Long = engine.framesPerBurst.toLong()

    override fun getPositionUs(): Long {
        val rate = engine.actualSampleRate.takeIf { it > 0 } ?: sampleRate
        return if (rate > 0) engine.positionFrames * C.MICROS_PER_SECOND / rate else 0L
    }

    override fun getPlaybackParameters(): PlaybackParameters = playbackParameters

    override fun isStalled(): Boolean = stalled

    override fun addListener(listener: AudioOutput.Listener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: AudioOutput.Listener) {
        listeners.remove(listener)
    }

    override fun setPlaybackParameters(playbackParameters: PlaybackParameters) {
        this.playbackParameters = PlaybackParameters.DEFAULT
    }

    override fun setOffloadDelayPadding(delayInFrames: Int, paddingInFrames: Int) {
        // Not offloaded.
    }

    override fun setOffloadEndOfStream() {
        // Not offloaded.
    }

    override fun setPlayerId(playerId: PlayerId) {
        // No-op for this output.
    }

    override fun attachAuxEffect(effectId: Int) {
        // Effects would modify PCM; rejected on this output.
    }

    override fun setAuxEffectSendLevel(level: Float) {
        // Not supported by Oboe output.
    }

    override fun setPreferredDevice(preferredDevice: AudioDeviceInfo?) {
        // Device is pinned at stream open via AudioRoute.audioDeviceId.
    }
}
