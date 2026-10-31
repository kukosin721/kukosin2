#include <jni.h>

#include <new>

#include <android/log.h>

#include "audio/AudioEngine.h"

#define LOG_TAG "AudioEngineJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Stable integer protocol shared with com.kukosin.app.audio.AudioEngine.
//
// Driver code:   0 = AAudio, 1 = OpenSL ES, -1 = Auto (unspecified).
// Format code:   0 = PCM I16, 1 = PCM I24, 2 = PCM Float 32, 3 = PCM I32.
// Sharing code:  0 = Shared, 1 = Exclusive.
// Performance:   0 = None, 1 = PowerSaving, 2 = LowLatency.
// Source mode:   0 = Tone, 1 = Pcm.
// Output path:   0 = Direct, 1 = Integrated exclusive AAudio, 2 = USB DAC.

namespace {

audio::AudioEngine* engineFrom(jlong handle) {
    return reinterpret_cast<audio::AudioEngine*>(handle);
}

oboe::AudioApi toAudioApi(jint code) {
    switch (code) {
        case 0: return oboe::AudioApi::AAudio;
        case 1: return oboe::AudioApi::OpenSLES;
        default: return oboe::AudioApi::Unspecified;
    }
}

oboe::AudioFormat toAudioFormat(jint code) {
    switch (code) {
        case 0: return oboe::AudioFormat::I16;
        case 1: return oboe::AudioFormat::I24;
        case 2: return oboe::AudioFormat::Float;
        case 3: return oboe::AudioFormat::I32;
        default: return oboe::AudioFormat::Unspecified;
    }
}

oboe::SharingMode toSharingMode(jint code) {
    return code == 1 ? oboe::SharingMode::Exclusive : oboe::SharingMode::Shared;
}

oboe::PerformanceMode toPerformanceMode(jint code) {
    switch (code) {
        case 1: return oboe::PerformanceMode::PowerSaving;
        case 2: return oboe::PerformanceMode::LowLatency;
        default: return oboe::PerformanceMode::None;
    }
}

jint formatToCode(oboe::AudioFormat format) {
    switch (format) {
        case oboe::AudioFormat::I16: return 0;
        case oboe::AudioFormat::I24: return 1;
        case oboe::AudioFormat::Float: return 2;
        case oboe::AudioFormat::I32: return 3;
        default: return 2;
    }
}

} // namespace

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_kukosin_app_audio_AudioEngine_nativeCreate(JNIEnv* /*env*/, jobject /*thiz*/) {
    auto* engine = new (std::nothrow) audio::AudioEngine();
    if (engine == nullptr) {
        LOGE("out of memory creating AudioEngine");
        return 0;
    }
    return reinterpret_cast<jlong>(engine);
}

JNIEXPORT void JNICALL
Java_com_kukosin_app_audio_AudioEngine_nativeDelete(JNIEnv* /*env*/, jobject /*thiz*/,
                                                    jlong handle) {
    delete engineFrom(handle);
}

JNIEXPORT jint JNICALL
Java_com_kukosin_app_audio_AudioEngine_nativeConfigure(JNIEnv* /*env*/, jobject /*thiz*/,
                                                       jlong handle, jint api, jint format,
                                                       jint sampleRate, jint channelCount,
                                                       jint sharingMode, jint performanceMode,
                                                       jint deviceId, jboolean bitPerfect,
                                                       jint outputPath) {
    auto* engine = engineFrom(handle);
    if (engine == nullptr) {
        return static_cast<jint>(oboe::Result::ErrorNull);
    }

    audio::EngineConfig config;
    config.audioApi = toAudioApi(api);
    config.format = toAudioFormat(format);
    config.sampleRate = sampleRate > 0 ? sampleRate : 0;
    config.channelCount = channelCount > 0 ? channelCount : oboe::ChannelCount::Stereo;
    config.sharingMode = toSharingMode(sharingMode);
    config.performanceMode = toPerformanceMode(performanceMode);
    config.deviceId = deviceId;
    config.bitPerfect = bitPerfect == JNI_TRUE;
    switch (outputPath) {
        case 1: config.outputPath = audio::OutputPath::kIntegratedExclusive; break;
        case 2: config.outputPath = audio::OutputPath::kUsbDac; break;
        default: config.outputPath = audio::OutputPath::kDirect; break;
    }

    const oboe::Result result = engine->open(config);
    return static_cast<jint>(result);
}

JNIEXPORT jint JNICALL
Java_com_kukosin_app_audio_AudioEngine_nativeStart(JNIEnv* /*env*/, jobject /*thiz*/,
                                                   jlong handle) {
    auto* engine = engineFrom(handle);
    if (engine == nullptr) {
        return static_cast<jint>(oboe::Result::ErrorNull);
    }
    return static_cast<jint>(engine->start());
}

JNIEXPORT jint JNICALL
Java_com_kukosin_app_audio_AudioEngine_nativeStop(JNIEnv* /*env*/, jobject /*thiz*/,
                                                  jlong handle) {
    auto* engine = engineFrom(handle);
    if (engine == nullptr) {
        return static_cast<jint>(oboe::Result::ErrorNull);
    }
    return static_cast<jint>(engine->stop());
}

JNIEXPORT jint JNICALL
Java_com_kukosin_app_audio_AudioEngine_nativePause(JNIEnv* /*env*/, jobject /*thiz*/,
                                                   jlong handle) {
    auto* engine = engineFrom(handle);
    if (engine == nullptr) {
        return static_cast<jint>(oboe::Result::ErrorNull);
    }
    return static_cast<jint>(engine->pause());
}

JNIEXPORT jint JNICALL
Java_com_kukosin_app_audio_AudioEngine_nativeFlush(JNIEnv* /*env*/, jobject /*thiz*/,
                                                   jlong handle) {
    auto* engine = engineFrom(handle);
    if (engine == nullptr) {
        return static_cast<jint>(oboe::Result::ErrorNull);
    }
    return static_cast<jint>(engine->flush());
}

JNIEXPORT void JNICALL
Java_com_kukosin_app_audio_AudioEngine_nativeSetVolume(JNIEnv* /*env*/, jobject /*thiz*/,
                                                       jlong handle, jfloat volume) {
    auto* engine = engineFrom(handle);
    if (engine != nullptr) {
        engine->setVolume(volume);
    }
}

JNIEXPORT void JNICALL
Java_com_kukosin_app_audio_AudioEngine_nativeSetSourceMode(JNIEnv* /*env*/, jobject /*thiz*/,
                                                           jlong handle, jint mode) {
    auto* engine = engineFrom(handle);
    if (engine != nullptr) {
        engine->setSourceMode(mode == 1 ? audio::SourceMode::kPcm : audio::SourceMode::kTone);
    }
}

JNIEXPORT jint JNICALL
Java_com_kukosin_app_audio_AudioEngine_nativeWritePacked(JNIEnv* env, jobject /*thiz*/,
                                                         jlong handle, jobject buffer,
                                                         jint offset, jint length) {
    auto* engine = engineFrom(handle);
    if (engine == nullptr || buffer == nullptr || length <= 0 || offset < 0) {
        return 0;
    }
    void* base = env->GetDirectBufferAddress(buffer);
    if (base == nullptr) {
        return 0;
    }
    const jlong capacity = env->GetDirectBufferCapacity(buffer);
    if (capacity < 0 || static_cast<jlong>(offset) + static_cast<jlong>(length) > capacity) {
        return 0;
    }
    const size_t written = engine->writePacked(
            static_cast<const uint8_t*>(base) + offset, static_cast<size_t>(length));
    return static_cast<jint>(written);
}

JNIEXPORT jint JNICALL
Java_com_kukosin_app_audio_AudioEngine_nativeWritePcm(JNIEnv* env, jobject /*thiz*/,
                                                      jlong handle, jfloatArray samples,
                                                      jint sampleCount) {
    auto* engine = engineFrom(handle);
    if (engine == nullptr || sampleCount <= 0) {
        return 0;
    }

    jfloat* data = env->GetFloatArrayElements(samples, nullptr);
    if (data == nullptr) {
        return 0;
    }
    const size_t written = engine->writePcm(data, static_cast<size_t>(sampleCount));
    env->ReleaseFloatArrayElements(samples, data, JNI_ABORT);
    return static_cast<jint>(written);
}

JNIEXPORT jint JNICALL
Java_com_kukosin_app_audio_AudioEngine_nativeGetState(JNIEnv* /*env*/, jobject /*thiz*/,
                                                      jlong handle) {
    auto* engine = engineFrom(handle);
    if (engine == nullptr) {
        return static_cast<jint>(oboe::StreamState::Closed);
    }
    return static_cast<jint>(engine->state());
}

JNIEXPORT jint JNICALL
Java_com_kukosin_app_audio_AudioEngine_nativeGetActualSampleRate(JNIEnv* /*env*/, jobject /*thiz*/,
                                                                 jlong handle) {
    auto* engine = engineFrom(handle);
    if (engine == nullptr) {
        return 0;
    }
    return engine->actualSampleRate();
}

JNIEXPORT jint JNICALL
Java_com_kukosin_app_audio_AudioEngine_nativeGetActualFormat(JNIEnv* /*env*/, jobject /*thiz*/,
                                                             jlong handle) {
    auto* engine = engineFrom(handle);
    if (engine == nullptr) {
        return 2;
    }
    return formatToCode(engine->actualFormat());
}

JNIEXPORT jint JNICALL
Java_com_kukosin_app_audio_AudioEngine_nativeGetFramesPerBurst(JNIEnv* /*env*/, jobject /*thiz*/,
                                                               jlong handle) {
    auto* engine = engineFrom(handle);
    if (engine == nullptr) {
        return 0;
    }
    return engine->framesPerBurst();
}

JNIEXPORT jlong JNICALL
Java_com_kukosin_app_audio_AudioEngine_nativeGetPositionFrames(JNIEnv* /*env*/, jobject /*thiz*/,
                                                                  jlong handle) {
    auto* engine = engineFrom(handle);
    if (engine == nullptr) {
        return 0;
    }
    return static_cast<jlong>(engine->framesRendered());
}

JNIEXPORT jint JNICALL
Java_com_kukosin_app_audio_AudioEngine_nativeGetSharingMode(JNIEnv* /*env*/, jobject /*thiz*/,
                                                            jlong handle) {
    auto* engine = engineFrom(handle);
    if (engine == nullptr) {
        return 0;
    }
    return engine->actualSharingMode() == oboe::SharingMode::Exclusive ? 1 : 0;
}

JNIEXPORT jint JNICALL
Java_com_kukosin_app_audio_AudioEngine_nativeGetActualDeviceId(JNIEnv* /*env*/, jobject /*thiz*/,
                                                               jlong handle) {
    auto* engine = engineFrom(handle);
    if (engine == nullptr) {
        return 0;
    }
    return engine->actualDeviceId();
}

JNIEXPORT jboolean JNICALL
Java_com_kukosin_app_audio_AudioEngine_nativeGetOpenedBitPerfect(JNIEnv* /*env*/, jobject /*thiz*/,
                                                                 jlong handle) {
    auto* engine = engineFrom(handle);
    if (engine == nullptr) {
        return JNI_FALSE;
    }
    return engine->openedBitPerfect() ? JNI_TRUE : JNI_FALSE;
}

} // extern "C"
