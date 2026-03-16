package com.audiofp.audio;

/**
 * Radix-2 Cooley-Tukey FFT (Princeton algorithm, refactored).
 *
 * Key improvements over the legacy version:
 *  - Hann windowing applied before transform to reduce spectral leakage
 *  - Zero-padding to next power of 2 handled as a distinct step
 *  - Returns a ready-to-use magnitude spectrum (frequency → amplitude pairs)
 *  - Non-power-of-2 inputs produce a clear error instead of silently giving
 *    wrong results
 */
public final class FFT {

    private FFT() {}

    // -------------------------------------------------------------------------
    // Core transform
    // -------------------------------------------------------------------------

    /**
     * Computes the DFT of {@code x} using the radix-2 Cooley-Tukey algorithm.
     *
     * @param x input array; length must be a power of 2
     * @return  complex spectrum of the same length
     * @throws IllegalArgumentException if x.length is not a power of 2
     */
    public static Complex[] fft(Complex[] x) {
        int n = x.length;
        if (n == 1) return new Complex[]{x[0]};
        if ((n & (n - 1)) != 0) {
            throw new IllegalArgumentException(
                    "FFT length must be a power of 2, got: " + n);
        }

        Complex[] even = new Complex[n / 2];
        Complex[] odd  = new Complex[n / 2];
        for (int k = 0; k < n / 2; k++) {
            even[k] = x[2 * k];
            odd[k]  = x[2 * k + 1];
        }

        Complex[] q = fft(even);
        Complex[] r = fft(odd);

        Complex[] y = new Complex[n];
        for (int k = 0; k < n / 2; k++) {
            double angle = -2.0 * Math.PI * k / n;
            Complex wk = new Complex(Math.cos(angle), Math.sin(angle));
            y[k]         = q[k].add(wk.mult(r[k]));
            y[k + n / 2] = q[k].sub(wk.mult(r[k]));
        }
        return y;
    }

    // -------------------------------------------------------------------------
    // Windowing & padding helpers
    // -------------------------------------------------------------------------

    /**
     * Applies a Hann window in-place to reduce spectral leakage.
     * This is critical for analysing short audio clips — without it, sharp
     * edges at the start/end of the buffer bleed energy across all bins.
     */
    public static double[] applyHannWindow(double[] samples) {
        int n = samples.length;
        double[] windowed = new double[n];
        for (int i = 0; i < n; i++) {
            double w = 0.5 * (1.0 - Math.cos(2.0 * Math.PI * i / (n - 1)));
            windowed[i] = samples[i] * w;
        }
        return windowed;
    }

    /**
     * Returns a copy of {@code samples} zero-padded to the next power of 2.
     * If the length is already a power of 2 the original array is returned
     * unchanged to avoid unnecessary allocation.
     */
    public static double[] zeroPad(double[] samples) {
        int n = samples.length;
        int nextPow2 = 1;
        while (nextPow2 < n) nextPow2 <<= 1;
        if (nextPow2 == n) return samples;
        double[] padded = new double[nextPow2];
        System.arraycopy(samples, 0, padded, 0, n);
        return padded;
    }

    // -------------------------------------------------------------------------
    // High-level spectrum computation
    // -------------------------------------------------------------------------

    /**
     * Converts time-domain samples to a magnitude spectrum.
     *
     * <ol>
     *   <li>Applies a Hann window</li>
     *   <li>Zero-pads to the next power of 2</li>
     *   <li>Runs the FFT</li>
     *   <li>Returns the positive-frequency half as {@code double[i][2]}
     *       where {@code [i][0]} = frequency in Hz and {@code [i][1]} = magnitude</li>
     * </ol>
     *
     * @param samples    raw PCM samples (mono, any scale)
     * @param sampleRate sample rate in Hz (e.g. 44100)
     */
    public static double[][] computeMagnitudeSpectrum(double[] samples, int sampleRate) {
        double[] windowed = applyHannWindow(samples);
        double[] padded   = zeroPad(windowed);
        int n = padded.length;

        Complex[] cinput = new Complex[n];
        for (int i = 0; i < n; i++) {
            cinput[i] = new Complex(padded[i], 0.0);
        }

        Complex[] spectrum = fft(cinput);

        double freqRes = (double) sampleRate / n;
        int halfN = n / 2;
        double[][] result = new double[halfN][2];
        for (int i = 0; i < halfN; i++) {
            result[i][0] = i * freqRes;
            result[i][1] = spectrum[i].abs();
        }
        return result;
    }
}
