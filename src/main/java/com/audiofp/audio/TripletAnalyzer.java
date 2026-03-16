package com.audiofp.audio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Extracts spectral "triplets" from an audio spectrum.
 *
 * A triplet is a (trough → peak → trough) pattern in the magnitude spectrum,
 * analogous to one "landmark" in Shazam-style fingerprinting. Only peaks
 * whose amplitude exceeds {@value #AMPLITUDE_THRESHOLD} × max are kept.
 *
 * This is a full rewrite of the original MainRunner triplet-extraction loop:
 *  - Extracted into its own testable class
 *  - Cleaner peak/trough detection with dedicated helpers
 *  - No mutable shared state
 *  - The critical integer-division bug in {@code df} has been eliminated
 *    (df is now computed as a pure double division from the start)
 */
public final class TripletAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(TripletAnalyzer.class);

    public static final double MIN_FREQ           = 20.0;    // Hz
    public static final double MAX_FREQ           = 5_000.0; // Hz
    public static final double AMPLITUDE_THRESHOLD = 0.30;   // fraction of peak

    private TripletAnalyzer() {}

    // -------------------------------------------------------------------------
    // Public record
    // -------------------------------------------------------------------------

    /**
     * A spectral triplet: the trough-peak-trough landmark used for matching.
     *
     * @param troughFreq1 frequency of the left trough (Hz)
     * @param troughAmp1  magnitude of the left trough
     * @param peakFreq    frequency of the central peak (Hz)
     * @param peakAmp     magnitude of the central peak
     * @param troughFreq2 frequency of the right trough (Hz)
     * @param troughAmp2  magnitude of the right trough
     */
    public record Triplet(
            double troughFreq1, double troughAmp1,
            double peakFreq,    double peakAmp,
            double troughFreq2, double troughAmp2) {

        /** Amplitude array [left-trough, peak, right-trough] for correlation. */
        public double[] amplitudeArray() {
            return new double[]{troughAmp1, peakAmp, troughAmp2};
        }

        /** Frequency array [left-trough, peak, right-trough] for correlation. */
        public double[] frequencyArray() {
            return new double[]{troughFreq1, peakFreq, troughFreq2};
        }
    }

    // -------------------------------------------------------------------------
    // Entry points
    // -------------------------------------------------------------------------

    /**
     * Extracts triplets from raw PCM samples.
     *
     * @param samples    mono PCM in any numeric range
     * @param sampleRate sample rate of {@code samples}
     */
    public static List<Triplet> extractTriplets(double[] samples, int sampleRate) {
        double[][] spectrum = FFT.computeMagnitudeSpectrum(samples, sampleRate);
        return extractTripletsFromSpectrum(spectrum);
    }

    /** Convenience overload using the canonical 44 100 Hz sample rate. */
    public static List<Triplet> extractTriplets(double[] samples) {
        return extractTriplets(samples, AudioCapture.SAMPLE_RATE);
    }

    /**
     * Extracts triplets from a pre-computed magnitude spectrum.
     *
     * @param spectrum array of [frequency_hz, magnitude] pairs (positive freqs only)
     */
    public static List<Triplet> extractTripletsFromSpectrum(double[][] spectrum) {
        // Collect bins in the target frequency range
        List<double[]> inRange = new ArrayList<>();
        for (double[] bin : spectrum) {
            if (bin[0] >= MIN_FREQ && bin[0] <= MAX_FREQ) {
                inRange.add(bin);
            }
        }
        if (inRange.isEmpty()) {
            log.warn("No spectrum bins in range [{}, {}] Hz", MIN_FREQ, MAX_FREQ);
            return List.of();
        }

        double[] magnitudes  = inRange.stream().mapToDouble(b -> b[1]).toArray();
        double[] frequencies = inRange.stream().mapToDouble(b -> b[0]).toArray();
        int n = magnitudes.length;

        double maxAmp = 0;
        for (double m : magnitudes) if (m > maxAmp) maxAmp = m;

        if (maxAmp == 0) {
            log.warn("All magnitudes are zero — silent input?");
            return List.of();
        }

        List<Triplet> triplets = new ArrayList<>();
        int i = 1;
        while (i < n - 1) {
            boolean isPeak = magnitudes[i] > magnitudes[i - 1]
                          && magnitudes[i] > magnitudes[i + 1];

            if (isPeak && magnitudes[i] >= maxAmp * AMPLITUDE_THRESHOLD) {
                int leftTrough  = findPrecedingTrough(magnitudes, i);
                int rightTrough = findFollowingTrough(magnitudes, i, n);

                triplets.add(new Triplet(
                        frequencies[leftTrough],  magnitudes[leftTrough],
                        frequencies[i],           magnitudes[i],
                        frequencies[rightTrough], magnitudes[rightTrough]));

                i = rightTrough + 1;
                continue;
            }
            i++;
        }

        log.debug("Extracted {} triplets (maxAmp={})", triplets.size(), maxAmp);
        return triplets;
    }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    /**
     * Returns the peak frequency from the triplet with the highest amplitude,
     * or 0.0 if the list is empty.
     */
    public static double findDominantFrequency(List<Triplet> triplets) {
        return triplets.stream()
                .max((a, b) -> Double.compare(a.peakAmp(), b.peakAmp()))
                .map(Triplet::peakFreq)
                .orElse(0.0);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private static int findPrecedingTrough(double[] mag, int peakIdx) {
        for (int i = peakIdx - 1; i >= 1; i--) {
            if (mag[i] < mag[i - 1] && mag[i] < mag[i + 1]) return i;
        }
        return 0;
    }

    private static int findFollowingTrough(double[] mag, int peakIdx, int n) {
        for (int i = peakIdx + 1; i < n - 1; i++) {
            if (mag[i] < mag[i - 1] && mag[i] < mag[i + 1]) return i;
        }
        return n - 1;
    }
}
