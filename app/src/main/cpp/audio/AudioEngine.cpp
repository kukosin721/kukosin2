#include "audio/AudioEngine.h"

#include <algorithm>
#include <cmath>
#include <cstring>
#include <cstdarg>

#include <android/log.h>

namespace audio {

namespace {
constexpr const char* kLogTag = "AudioEngine";

void logI(const char* fmt, ...) __attribute__((format(printf, 1, 2)));
void logI(const char* fmt, ...) {
    va_list args;
    va_start(args, fmt);
    __android_log_vprint(ANDROID_LOG_INFO, kLogTag, fmt, args);
    va_end(args);
}

void logE(const char* fmt, ...) __attribute__((format(printf, 1, 2)));
void logE(const char* fmt, ...) {
    va_list args;
    va_start(args, fmt);
    __android_log_vprint(ANDROID_LOG_ERROR, kLogTag, fmt, args);
    va_end(args);
}

const char* resultName(oboe::Result result) {
    return oboe::convertToText(result);
}

// ~2 s of stereo packed 24-bit at 192 kHz (~2.2 MB), rounded to 4 MB.
constexpr size_t kDefaultRingCapacity = 1u << 22;

inline float clamp1(float x) {
    return x < -1.0f ? -1.0f : (x > 1.0f ? 1.0f : x);
}

} // namespace

ByteRingBuffer::ByteRingBuffer(size_t capacity)
    : capacity_(capacity), mask_(capacity - 1), buffer_(capacity) {}

size_t ByteRingBuffer::write(const uint8_t* data, size_t count) {
    const size_t readIndex = readIndex_.load(std::memory_order_acquire);
    const size_t writeIndex = writeIndex_.load(std::memory_order_relaxed);
    const size_t freeSpace = capacity_ - (writeIndex - readIndex);
    const size_t n = std::min(count, freeSpace);
    const size_t w = writeIndex & mask_;
    const size_t first = std::min(n, capacity_ - w);
    std::memcpy(buffer_.data() + w, data, first);
    if (n > first) {
        std::memcpy(buffer_.data(), data + first, n - first);
    }
    writeIndex_.store(writeIndex + n, std::memory_order_release);
    return n;
}

size_t ByteRingBuffer::read(uint8_t* out, size_t count) {
    const size_t readIndex = readIndex_.load(std::memory_order_relaxed);
    const size_t writeIndex = writeIndex_.load(std::memory_order_acquire);
    const size_t available = writeIndex - readIndex;
    const size_t n = std::min(count, available);
    const size_t r = readIndex & mask_;
    const size_t first = std::min(n, capacity_ - r);
    std::memcpy(out, buffer_.data() + r, first);
    if (n > first) {
        std::memcpy(out + first, buffer_.data(), n - first);
    }
    readIndex_.store(readIndex + n, std::memory_order_release);
    return n;
}

void ByteRingBuffer::clear() {
    readIndex_.store(writeIndex_.load(std::memory_order_acquire), std::memory_order_release);
}

size_t ByteRingBuffer::availableToRead() const {
    const size_t writeIndex = writeIndex_.load(std::memory_order_acquire);
    const size_t readIndex = readIndex_.load(std::memory_order_acquire);
    return writeIndex - readIndex;
}

int32_t AudioEngine::bytesPerSample(oboe::AudioFormat format) {
    switch (format) {
        case oboe::AudioFormat::I16: return 2;
        case oboe::AudioFormat::I24: return 3;
        case oboe::AudioFormat::I32: return 4;
        case oboe::AudioFormat::Float: return 4;
        default: return 4;
    }
}

AudioEngine::AudioEngine() : mByteBuffer(kDefaultRingCapacity) {}

AudioEngine::~AudioEngine() {
    close();
}

oboe::Result AudioEngine::openStream(const EngineConfig& config,
                                     oboe::SharingMode sharing,
                                     bool allowConversion) {
    oboe::AudioStreamBuilder builder;
    builder.setDirection(oboe::Direction::Output);
    builder.setAudioApi(config.audioApi);
    builder.setFormat(config.format);
    if (config.sampleRate > 0) {
        builder.setSampleRate(config.sampleRate);
    }
    builder.setChannelCount(config.channelCount > 0 ? config.channelCount
                                                    : oboe::ChannelCount::Stereo);
    builder.setSharingMode(sharing);
    // Bit-perfect / USB / high-res MUST NOT use LowLatency: that path is the
    // Fast Mixer and typically locks 48 kHz with SRC. None uses the normal
    // mixer or a direct HAL track so the DAC keeps the requested rate.
    builder.setPerformanceMode(config.bitPerfect ? oboe::PerformanceMode::None
                                                 : config.performanceMode);
    if (config.deviceId > 0) {
        builder.setDeviceId(config.deviceId);
    }
    builder.setUsage(oboe::Usage::Media);
    builder.setContentType(oboe::ContentType::Music);
    builder.setSpatializationBehavior(oboe::SpatializationBehavior::Never);
    builder.setDataCallback(this);
    builder.setErrorCallback(this);

    if (!allowConversion) {
        builder.setFormatConversionAllowed(false);
        builder.setChannelConversionAllowed(false);
        builder.setSampleRateConversionQuality(oboe::SampleRateConversionQuality::None);
    } else {
        builder.setFormatConversionAllowed(true);
        builder.setChannelConversionAllowed(true);
    }

    if (config.bufferCapacityInFrames > 0) {
        builder.setBufferCapacityInFrames(config.bufferCapacityInFrames);
    }

    return builder.openStream(mStream);
}

oboe::Result AudioEngine::open(const EngineConfig& config) {
    close();
    mOpenedBitPerfect.store(false, std::memory_order_relaxed);

    const oboe::SharingMode requestedSharing =
            config.bitPerfect ? oboe::SharingMode::Exclusive : config.sharingMode;

    oboe::Result result = openStream(config, requestedSharing, /*allowConversion=*/false);
    if (result != oboe::Result::OK && requestedSharing == oboe::SharingMode::Exclusive) {
        logE("exclusive open failed: %s (%d); retrying shared, still no conversion",
             resultName(result), static_cast<int>(result));
        result = openStream(config, oboe::SharingMode::Shared, /*allowConversion=*/false);
    }
    if (result != oboe::Result::OK) {
        logE("openStream failed: %s (%d)", resultName(result), static_cast<int>(result));
        return result;
    }

    const int32_t channelCount = mStream->getChannelCount();
    const int32_t bufferCapacity = mStream->getBufferCapacityInFrames();
    mBytesPerFrame = channelCount * bytesPerSample(mStream->getFormat());
    mScratchSamples = static_cast<size_t>(bufferCapacity) * static_cast<size_t>(channelCount);
    mScratch = std::make_unique<float[]>(mScratchSamples);

    mToneSampleRate = mStream->getSampleRate();
    mTonePhaseIncrement = 2.0f * kPi * kToneFrequencyHz / static_cast<float>(mToneSampleRate);
    mTonePhase = 0.0f;
    mFramesRendered.store(0, std::memory_order_relaxed);
    mByteBuffer.clear();

    // Prefer a stable high-res buffer over minimum latency.
    const int32_t burst = mStream->getFramesPerBurst();
    const int32_t cap = mStream->getBufferCapacityInFrames();
    if (burst > 0 && cap > 0) {
        const int32_t multiple = config.outputPath == OutputPath::kUsbDac ? 8 : 4;
        const int32_t target = std::min(cap, burst * multiple);
        mStream->setBufferSizeInFrames(target);
    }

    const bool formatOk = mStream->getFormat() == config.format ||
                          config.format == oboe::AudioFormat::Unspecified;
    const bool rateOk = config.sampleRate <= 0 ||
                        mStream->getSampleRate() == config.sampleRate;
    const bool exclusive = mStream->getSharingMode() == oboe::SharingMode::Exclusive;
    mOpenedBitPerfect.store(config.bitPerfect && formatOk && rateOk, std::memory_order_relaxed);

    logI("opened stream: api=%d format=%d rate=%d ch=%d burst=%d share=%d device=%d exclusive=%d bitPerfect=%d path=%d",
         static_cast<int>(mStream->getAudioApi()),
         static_cast<int>(mStream->getFormat()),
         mToneSampleRate,
         channelCount,
         burst,
         static_cast<int>(mStream->getSharingMode()),
         mStream->getDeviceId(),
         exclusive ? 1 : 0,
         mOpenedBitPerfect.load(std::memory_order_relaxed) ? 1 : 0,
         static_cast<int>(config.outputPath));

    return oboe::Result::OK;
}

oboe::Result AudioEngine::close() {
    if (mStream) {
        mStream->close();
        mStream.reset();
    }
    mScratch.reset();
    mScratchSamples = 0;
    mByteBuffer.clear();
    mOpenedBitPerfect.store(false, std::memory_order_relaxed);
    return oboe::Result::OK;
}

oboe::Result AudioEngine::requestStateTransition(oboe::StreamState target) {
    if (!mStream) {
        return oboe::Result::ErrorInvalidState;
    }
    oboe::Result result = oboe::Result::OK;
    auto* stream = mStream.get();
    switch (target) {
        case oboe::StreamState::Started:
            result = stream->requestStart();
            break;
        case oboe::StreamState::Paused:
            result = stream->requestPause();
            break;
        case oboe::StreamState::Stopped:
            result = stream->requestStop();
            break;
        case oboe::StreamState::Flushed:
            result = stream->requestFlush();
            break;
        default:
            result = oboe::Result::ErrorInvalidState;
            break;
    }
    if (result != oboe::Result::OK) {
        logE("state transition failed: %s (%d)", resultName(result), static_cast<int>(result));
    }
    return result;
}

oboe::Result AudioEngine::start() { return requestStateTransition(oboe::StreamState::Started); }
oboe::Result AudioEngine::stop() { return requestStateTransition(oboe::StreamState::Stopped); }
oboe::Result AudioEngine::pause() { return requestStateTransition(oboe::StreamState::Paused); }
oboe::Result AudioEngine::flush() {
    mByteBuffer.clear();
    mFramesRendered.store(0, std::memory_order_relaxed);
    return requestStateTransition(oboe::StreamState::Flushed);
}

oboe::StreamState AudioEngine::state() const {
    return mStream ? mStream->getState() : oboe::StreamState::Closed;
}

int32_t AudioEngine::actualSampleRate() const {
    return mStream ? mStream->getSampleRate() : 0;
}

oboe::AudioFormat AudioEngine::actualFormat() const {
    return mStream ? mStream->getFormat() : oboe::AudioFormat::Unspecified;
}

int32_t AudioEngine::framesPerBurst() const {
    return mStream ? mStream->getFramesPerBurst() : 0;
}

int32_t AudioEngine::actualDeviceId() const {
    return mStream ? mStream->getDeviceId() : 0;
}

oboe::SharingMode AudioEngine::actualSharingMode() const {
    return mStream ? mStream->getSharingMode() : oboe::SharingMode::Shared;
}

size_t AudioEngine::writePacked(const uint8_t* data, size_t byteCount) {
    return mByteBuffer.write(data, byteCount);
}

size_t AudioEngine::writePcm(const float* samples, size_t count) {
    if (samples == nullptr || count == 0 || !mStream) {
        return 0;
    }
    const oboe::AudioFormat format = mStream->getFormat();
    const int32_t bps = bytesPerSample(format);
    if (format == oboe::AudioFormat::Float) {
        return mByteBuffer.write(reinterpret_cast<const uint8_t*>(samples), count * 4) / 4;
    }
    std::vector<uint8_t> packed(count * static_cast<size_t>(bps));
    convertAndWrite(samples, packed.data(), static_cast<int32_t>(count), format);
    return mByteBuffer.write(packed.data(), packed.size()) / static_cast<size_t>(bps);
}

oboe::DataCallbackResult AudioEngine::onAudioReady(oboe::AudioStream* stream,
                                                   void* audioData,
                                                   int32_t numFrames) {
    const int32_t channelCount = stream->getChannelCount();
    const int32_t sampleCount = numFrames * channelCount;
    const int32_t frameBytes = mBytesPerFrame > 0
            ? mBytesPerFrame
            : channelCount * bytesPerSample(stream->getFormat());
    const size_t byteCount = static_cast<size_t>(numFrames) * static_cast<size_t>(frameBytes);

    if (mSourceMode.load(std::memory_order_relaxed) == SourceMode::kPcm) {
        // Packed passthrough: copy decoded PCM as-is. No gain, no resample.
        const size_t got = mByteBuffer.read(static_cast<uint8_t*>(audioData), byteCount);
        if (got < byteCount) {
            std::memset(static_cast<uint8_t*>(audioData) + got, 0, byteCount - got);
        }
    } else {
        if (static_cast<size_t>(sampleCount) > mScratchSamples || mScratch == nullptr) {
            std::memset(audioData, 0, byteCount);
            return oboe::DataCallbackResult::Continue;
        }
        float* scratch = mScratch.get();
        renderTone(scratch, sampleCount);
        convertAndWrite(scratch, audioData, sampleCount, stream->getFormat());
    }

    mFramesRendered.fetch_add(numFrames, std::memory_order_relaxed);
    return oboe::DataCallbackResult::Continue;
}

void AudioEngine::renderTone(float* out, int32_t sampleCount) {
    const float volume = mVolume.load(std::memory_order_relaxed);
    const float increment = mTonePhaseIncrement;
    float phase = mTonePhase;
    for (int32_t i = 0; i < sampleCount; ++i) {
        out[i] = std::sin(phase) * 0.2f * volume;
        phase += increment;
        if (phase >= 2.0f * kPi) {
            phase -= 2.0f * kPi;
        }
    }
    mTonePhase = phase;
}

void AudioEngine::convertAndWrite(const float* src, void* dst, int32_t sampleCount,
                                  oboe::AudioFormat format) {
    switch (format) {
        case oboe::AudioFormat::Float: {
            auto* out = static_cast<float*>(dst);
            for (int32_t i = 0; i < sampleCount; ++i) {
                out[i] = src[i];
            }
            break;
        }
        case oboe::AudioFormat::I16: {
            auto* out = static_cast<int16_t*>(dst);
            for (int32_t i = 0; i < sampleCount; ++i) {
                out[i] = static_cast<int16_t>(clamp1(src[i]) * 32767.0f);
            }
            break;
        }
        case oboe::AudioFormat::I24: {
            auto* out = static_cast<uint8_t*>(dst);
            for (int32_t i = 0; i < sampleCount; ++i) {
                const float s = clamp1(src[i]);
                int32_t v = static_cast<int32_t>(std::rint(s * 8388608.0f));
                v = std::min(8388607, std::max(-8388608, v));
                out[i * 3 + 0] = static_cast<uint8_t>(v & 0xFF);
                out[i * 3 + 1] = static_cast<uint8_t>((v >> 8) & 0xFF);
                out[i * 3 + 2] = static_cast<uint8_t>((v >> 16) & 0xFF);
            }
            break;
        }
        case oboe::AudioFormat::I32: {
            auto* out = static_cast<int32_t*>(dst);
            for (int32_t i = 0; i < sampleCount; ++i) {
                out[i] = static_cast<int32_t>(clamp1(src[i]) * 2147483647.0);
            }
            break;
        }
        default: {
            std::fill_n(static_cast<uint8_t*>(dst),
                        static_cast<size_t>(sampleCount) * static_cast<size_t>(bytesPerSample(format)),
                        static_cast<uint8_t>(0));
            break;
        }
    }
}

void AudioEngine::onErrorAfterClose(oboe::AudioStream* /*stream*/, oboe::Result error) {
    logE("stream error after close: %s (%d)", resultName(error), static_cast<int>(error));
}

} // namespace audio
