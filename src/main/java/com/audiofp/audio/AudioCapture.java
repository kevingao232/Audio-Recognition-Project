package com.audiofp.audio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Utility class for capturing audio from the system microphone and
 * converting raw audio bytes into double-precision sample arrays.
 *
 * All audio is normalised to: 44 100 Hz, 16-bit signed, mono, little-endian.
 */
public final class AudioCapture {

    private static final Logger log = LoggerFactory.getLogger(AudioCapture.class);

    public static final int SAMPLE_RATE      = 44_100;
    public static final int SAMPLE_BITS      = 16;
    public static final int CHANNELS         = 1;
    public static final int RECORD_SECONDS   = 10;

    private AudioCapture() {}

    // -------------------------------------------------------------------------
    // Capture
    // -------------------------------------------------------------------------

    /** Returns the canonical {@link AudioFormat} used throughout this app. */
    public static AudioFormat getFormat() {
        return new AudioFormat(SAMPLE_RATE, SAMPLE_BITS, CHANNELS, true, false);
    }

    /**
     * Records {@value #RECORD_SECONDS} seconds from the default microphone.
     *
     * @return mono PCM samples in the range [−32 768, 32 767]
     * @throws LineUnavailableException if no microphone is accessible
     */
    public static double[] recordFromMicrophone() throws LineUnavailableException {
        AudioFormat format = getFormat();
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        if (!AudioSystem.isLineSupported(info)) {
            throw new LineUnavailableException(
                    "A microphone (TargetDataLine) is not supported on this system.");
        }

        TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
        line.open(format);
        line.start();

        log.info("Recording for {} seconds…", RECORD_SECONDS);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        long deadline = System.currentTimeMillis() + RECORD_SECONDS * 1_000L;

        try {
            while (System.currentTimeMillis() < deadline) {
                int count = line.read(buffer, 0, buffer.length);
                if (count > 0) out.write(buffer, 0, count);
            }
        } finally {
            line.stop();
            line.close();
        }

        log.info("Recording complete — {} bytes captured.", out.size());
        return bytesToDoubles(out.toByteArray());
    }

    // -------------------------------------------------------------------------
    // Format conversion
    // -------------------------------------------------------------------------

    /**
     * Decodes audio bytes into a double[] sample array.
     *
     * Supports:
     * <ul>
     *   <li>WAV / AIFF files — decoded via {@link AudioSystem} with automatic
     *       format conversion to the canonical format</li>
     *   <li>Raw 16-bit signed little-endian PCM — passed through directly</li>
     * </ul>
     */
    public static double[] readAudioBytes(byte[] bytes)
            throws IOException, UnsupportedAudioFileException {
        if (isWavOrAiff(bytes)) {
            return decodeAudioFile(bytes);
        }
        // Assume raw 16-bit LE PCM
        return bytesToDoubles(bytes);
    }

    /**
     * Converts 16-bit signed little-endian PCM bytes to a double[] sample array.
     * Sample values are in the range [−32 768, 32 767].
     */
    public static double[] bytesToDoubles(byte[] pcmBytes) {
        ByteBuffer buf = ByteBuffer.wrap(pcmBytes).order(ByteOrder.LITTLE_ENDIAN);
        double[] samples = new double[pcmBytes.length / 2];
        for (int i = 0; i < samples.length; i++) {
            samples[i] = buf.getShort();
        }
        return samples;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private static boolean isWavOrAiff(byte[] bytes) {
        if (bytes.length < 4) return false;
        // RIFF header (WAV)
        boolean isRiff = bytes[0] == 'R' && bytes[1] == 'I'
                      && bytes[2] == 'F' && bytes[3] == 'F';
        // FORM header (AIFF)
        boolean isForm = bytes[0] == 'F' && bytes[1] == 'O'
                      && bytes[2] == 'R' && bytes[3] == 'M';
        return isRiff || isForm;
    }

    /**
     * Decodes a WAV/AIFF byte array to mono 16-bit PCM at 44 100 Hz.
     * Multi-channel audio is mixed down to mono by averaging channels.
     */
    private static double[] decodeAudioFile(byte[] fileBytes)
            throws IOException, UnsupportedAudioFileException {

        try (AudioInputStream raw = AudioSystem.getAudioInputStream(
                new ByteArrayInputStream(fileBytes))) {

            AudioFormat srcFormat = raw.getFormat();
            AudioFormat targetFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    SAMPLE_RATE, SAMPLE_BITS,
                    srcFormat.getChannels(),          // keep original channel count
                    srcFormat.getChannels() * 2,
                    SAMPLE_RATE, false);

            try (AudioInputStream converted =
                         AudioSystem.getAudioInputStream(targetFormat, raw)) {

                byte[] pcmBytes = converted.readAllBytes();
                int channels = targetFormat.getChannels();

                if (channels == 1) {
                    return bytesToDoubles(pcmBytes);
                }
                return mixDownToMono(pcmBytes, channels);
            }
        }
    }

    /** Mixes multi-channel 16-bit LE PCM down to mono by averaging channels. */
    private static double[] mixDownToMono(byte[] pcmBytes, int channels) {
        ByteBuffer buf = ByteBuffer.wrap(pcmBytes).order(ByteOrder.LITTLE_ENDIAN);
        int totalSamples = pcmBytes.length / 2;
        int monoSamples  = totalSamples / channels;
        double[] mono    = new double[monoSamples];

        for (int i = 0; i < monoSamples; i++) {
            double sum = 0;
            for (int c = 0; c < channels; c++) {
                sum += buf.getShort();
            }
            mono[i] = sum / channels;
        }
        return mono;
    }
}
