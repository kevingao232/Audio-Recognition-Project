package com.audiofp.audio;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FFTTest {

    // ── fft() ─────────────────────────────────────────────────────────────────

    @Test
    void fft_singleElement_returnsSameElement() {
        Complex[] x = { new Complex(3.0, -1.5) };
        Complex[] y = FFT.fft(x);
        assertEquals(1, y.length);
        assertEquals(3.0,  y[0].re, 1e-9);
        assertEquals(-1.5, y[0].im, 1e-9);
    }

    @Test
    void fft_nonPowerOfTwo_throwsIllegalArgument() {
        Complex[] x = new Complex[3];
        for (int i = 0; i < 3; i++) x[i] = new Complex(i, 0);
        assertThrows(IllegalArgumentException.class, () -> FFT.fft(x));
    }

    @Test
    void fft_pureCosineFindsPeakAtCorrectBin() {
        // A cos(2π·k₀·n/N) in the DFT should produce peaks at bins k₀ and N−k₀.
        int n  = 64;
        int k0 = 4;
        Complex[] x = new Complex[n];
        for (int i = 0; i < n; i++) {
            x[i] = new Complex(Math.cos(2 * Math.PI * k0 * i / n), 0);
        }

        Complex[] y   = FFT.fft(x);
        int peakBin   = 0;
        double peakMag = 0;
        for (int i = 0; i < n / 2; i++) {
            double mag = y[i].abs();
            if (mag > peakMag) { peakMag = mag; peakBin = i; }
        }

        assertEquals(k0, peakBin, "Peak should be at bin " + k0);
    }

    // ── zeroPad() ─────────────────────────────────────────────────────────────

    @Test
    void zeroPad_alreadyPowerOf2_returnsOriginal() {
        double[] x = {1, 2, 3, 4, 5, 6, 7, 8};
        assertSame(x, FFT.zeroPad(x));
    }

    @Test
    void zeroPad_padsToNextPowerOf2() {
        double[] x      = {1, 2, 3, 4, 5};
        double[] padded = FFT.zeroPad(x);
        assertEquals(8, padded.length);
        assertEquals(5.0, padded[4], 1e-9);
        assertEquals(0.0, padded[7], 1e-9);
    }

    // ── applyHannWindow() ─────────────────────────────────────────────────────

    @Test
    void hannWindow_edgesAreNearZero() {
        double[] x  = new double[1024];
        for (int i = 0; i < x.length; i++) x[i] = 1.0;
        double[] w  = FFT.applyHannWindow(x);

        assertTrue(w[0]    < 0.001, "First sample should be near zero");
        assertTrue(w[1023] < 0.001, "Last sample should be near zero");
    }

    @Test
    void hannWindow_centreIsNearOne() {
        double[] x = new double[1024];
        for (int i = 0; i < x.length; i++) x[i] = 1.0;
        double[] w = FFT.applyHannWindow(x);
        assertTrue(w[512] > 0.99, "Centre sample should be near 1.0");
    }

    // ── computeMagnitudeSpectrum() ────────────────────────────────────────────

    @Test
    void magnitudeSpectrum_sineWavePeaksNearCorrectFrequency() {
        int    sampleRate = 44_100;
        int    nSamples   = 4096;
        double targetHz   = 440.0;

        double[] samples = new double[nSamples];
        for (int i = 0; i < nSamples; i++) {
            samples[i] = Math.sin(2 * Math.PI * targetHz * i / sampleRate);
        }

        double[][] spectrum = FFT.computeMagnitudeSpectrum(samples, sampleRate);

        double peakFreq = 0, peakMag = 0;
        for (double[] bin : spectrum) {
            if (bin[1] > peakMag) { peakMag = bin[1]; peakFreq = bin[0]; }
        }

        assertEquals(targetHz, peakFreq, 20.0,
                "Peak frequency should be within 20 Hz of " + targetHz);
    }
}
