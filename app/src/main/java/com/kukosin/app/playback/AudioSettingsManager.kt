package com.kukosin.app.playback

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AudioConfig(
    val driver: String = "AAudio",
    val targetSampleRate: String = "Auto (Bit-Perfect)",
    val targetPcmFormat: String = "PCM Float 32-bit",
    val bitPerfectEnabled: Boolean = true,
    val autoAnalyze: Boolean = false,
    val decoder: String = "FFmpeg (Lossless)",
    val useOboeDirectOutput: Boolean = false,
    val queueCompactMode: Boolean = false,
    val showQueueBadge: Boolean = true,
    val queueShowHistory: Boolean = true,
) {
    val targetSampleRateHz: Int
        get() = when (targetSampleRate) {
            "44.1 kHz" -> 44100
            "48.0 kHz" -> 48000
            "88.2 kHz" -> 88200
            "96.0 kHz" -> 96000
            "176.4 kHz" -> 176400
            "192.0 kHz" -> 192000
            "384.0 kHz" -> 384000
            else -> 0
        }
}

object AudioSettingsManager {
    private const val PREFS_NAME = "kukosin_audio_settings"
    private var prefs: SharedPreferences? = null
    private val state = MutableStateFlow(AudioConfig())
    val configState: StateFlow<AudioConfig> = state.asStateFlow()

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            load()
        }
    }

    private fun load() {
        val p = prefs ?: return
        state.value = AudioConfig(
            driver = p.getString("driver", "AAudio") ?: "AAudio",
            targetSampleRate = p.getString("target_sample_rate", "Auto (Bit-Perfect)") ?: "Auto (Bit-Perfect)",
            targetPcmFormat = p.getString("target_pcm_format", "PCM Float 32-bit") ?: "PCM Float 32-bit",
            bitPerfectEnabled = p.getBoolean("bit_perfect_enabled", true),
            autoAnalyze = p.getBoolean("auto_analyze", false),
            decoder = p.getString("decoder", "FFmpeg (Lossless)") ?: "FFmpeg (Lossless)",
            useOboeDirectOutput = p.getBoolean("use_oboe_direct_output", false),
            queueCompactMode = p.getBoolean("queue_compact_mode", false),
            showQueueBadge = p.getBoolean("show_queue_badge", true),
            queueShowHistory = p.getBoolean("queue_show_history", true),
        )
    }

    fun setDriver(driver: String) {
        prefs?.edit()?.putString("driver", driver)?.apply()
        state.value = state.value.copy(driver = driver)
    }

    fun setTargetSampleRate(rate: String) {
        prefs?.edit()?.putString("target_sample_rate", rate)?.apply()
        state.value = state.value.copy(targetSampleRate = rate)
    }

    fun setTargetPcmFormat(format: String) {
        prefs?.edit()?.putString("target_pcm_format", format)?.apply()
        state.value = state.value.copy(targetPcmFormat = format)
    }

    fun setBitPerfectEnabled(enabled: Boolean) {
        prefs?.edit()?.putBoolean("bit_perfect_enabled", enabled)?.apply()
        state.value = state.value.copy(bitPerfectEnabled = enabled)
    }

    fun setAutoAnalyze(enabled: Boolean) {
        prefs?.edit()?.putBoolean("auto_analyze", enabled)?.apply()
        state.value = state.value.copy(autoAnalyze = enabled)
    }

    fun setDecoder(decoder: String) {
        prefs?.edit()?.putString("decoder", decoder)?.apply()
        state.value = state.value.copy(decoder = decoder)
    }

    fun setUseOboeDirectOutput(enabled: Boolean) {
        prefs?.edit()?.putBoolean("use_oboe_direct_output", enabled)?.apply()
        state.value = state.value.copy(useOboeDirectOutput = enabled)
    }

    fun setQueueCompactMode(compact: Boolean) {
        prefs?.edit()?.putBoolean("queue_compact_mode", compact)?.apply()
        state.value = state.value.copy(queueCompactMode = compact)
    }

    fun setShowQueueBadge(show: Boolean) {
        prefs?.edit()?.putBoolean("show_queue_badge", show)?.apply()
        state.value = state.value.copy(showQueueBadge = show)
    }

    fun setQueueShowHistory(show: Boolean) {
        prefs?.edit()?.putBoolean("queue_show_history", show)?.apply()
        state.value = state.value.copy(queueShowHistory = show)
    }
}
