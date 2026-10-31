#pragma once

#include <atomic>
#include <cstddef>
#include <cstdint>
#include <memory>
#include <vector>

#include <oboe/Oboe.h>

namespace audio {

// Single-producer / single-consumer lock-free ring of packed PCM bytes.
// Producer is the JNI write thread, consumer is the Oboe callback thread.
// Capacity must be a power of two.
class ByteRingBuffer {
public:
    explicit ByteRingBuffer(size_t capacity);
    ByteRingBuffer(const ByteRingBuffer&) = delete;
    ByteRingBuffer& operator=(const ByteRingBuffer&) = delete;

    size_t write(const uint8_t* data, size_t count);
    size_t read(uint8_t* out, size_t count);
    void clear();
    size_t availableToRead() const;
    size_t capacity() const { return capacity_; }

private:
    const size_t capacity_;
    const size_t mask_;
    std::vector<uint8_t> buffer_;
    std::atomic<size_t> readIndex_{0};
    std::atomic<size_t> writeIndex_{0};
};

enum class SourceMode : int32_t {
    kTone = 0,
    kPcm  = 1,
};

// 0 = pin preferred device (controlador directo)
// 1 = DAC integrado, AAudio Exclusive
// 2 = DAC USB, Exclusive sin SRC/mezcla del sistema
enum class OutputPath : int32_t {
    kDirect = 0,
    kIntegratedExclusive = 1,
    kUsbDac = 2,
};

struct EngineConfig {
    oboe::AudioApi audioApi = oboe::AudioApi::AAudio;
    oboe::AudioFormat format = oboe::AudioFormat::I16;
    int32_t sampleRate = 0;
    int32_t channelCount = oboe::ChannelCount::Stereo;
    oboe::SharingMode sharingMode = oboe::SharingMode::Exclusive;
    oboe::PerformanceMode performanceMode = oboe::PerformanceMode::None;
    int32_t deviceId = 0;
    int32_t bufferCapacityInFrames = 0;
    bool bitPerfect = true;
    OutputPath outputPath = OutputPath::kDirect;
};

// Low-level output engine. PCM playback is a packed-byte passthrough: the
// callback never converts, resamples or applies gain. Tone generation is the
// only path that synthesizes samples.
class AudioEngine final : public oboe::AudioStreamDataCallback,
                          public oboe::AudioStreamErrorCallback {
public:
    AudioEngine();
    ~AudioEngine() override;

    AudioEngine(const AudioEngine&) = delete;
    AudioEngine& operator=(const AudioEngine&) = delete;

    oboe::Result open(const EngineConfig& config);
    oboe::Result close();

    oboe::Result start();
    oboe::Result stop();
    oboe::Result pause();
    oboe::Result flush();

    void setVolume(float volume) {
        mVolume.store(volume < 0.0f ? 0.0f : (volume > 1.0f ? 1.0f : volume),
                      std::memory_order_relaxed);
    }

    void setSourceMode(SourceMode mode) { mSourceMode.store(mode, std::memory_order_relaxed); }

    // Float PCM helper (converts on the producer thread). Prefer writePacked.
    size_t writePcm(const float* samples, size_t count);

    // Bit-perfect packed PCM. Returns bytes accepted.
    size_t writePacked(const uint8_t* data, size_t byteCount);

    oboe::StreamState state() const;
    int32_t actualSampleRate() const;
    oboe::AudioFormat actualFormat() const;
    int32_t framesPerBurst() const;
    int32_t actualDeviceId() const;
    oboe::SharingMode actualSharingMode() const;
    bool openedBitPerfect() const { return mOpenedBitPerfect.load(std::memory_order_relaxed); }

    int64_t framesRendered() const { return mFramesRendered.load(std::memory_order_relaxed); }

    oboe::DataCallbackResult onAudioReady(oboe::AudioStream* stream,
                                          void* audioData,
                                          int32_t numFrames) override;

    void onErrorAfterClose(oboe::AudioStream* stream, oboe::Result error) override;

private:
    oboe::Result requestStateTransition(oboe::StreamState target);
    oboe::Result openStream(const EngineConfig& config,
                            oboe::SharingMode sharing,
                            bool allowConversion);

    void renderTone(float* out, int32_t sampleCount);
    void convertAndWrite(const float* src, void* dst, int32_t sampleCount, oboe::AudioFormat format);

    static int32_t bytesPerSample(oboe::AudioFormat format);

    static constexpr float kPi = 3.14159265358979323846f;
    static constexpr float kToneFrequencyHz = 440.0f;

    std::shared_ptr<oboe::AudioStream> mStream;
    ByteRingBuffer mByteBuffer;

    std::unique_ptr<float[]> mScratch;
    size_t mScratchSamples = 0;
    int32_t mBytesPerFrame = 4;

    std::atomic<SourceMode> mSourceMode{SourceMode::kTone};
    std::atomic<float> mVolume{1.0f};
    std::atomic<bool> mOpenedBitPerfect{false};

    float mTonePhase = 0.0f;
    float mTonePhaseIncrement = 0.0f;
    int32_t mToneSampleRate = 48000;

    std::atomic<int64_t> mFramesRendered{0};
};

} // namespace audio
