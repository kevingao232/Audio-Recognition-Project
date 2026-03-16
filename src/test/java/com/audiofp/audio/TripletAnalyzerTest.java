package com.audiofp.audio;

import com.audiofp.audio.TripletAnalyzer.Triplet;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TripletAnalyzerTest {

    @Test
    void extractTriplets_silentInput_returnsEmpty() {
        double[] samples = new double[4096]; // all zeros
        List<Triplet> triplets = TripletAnalyzer.extractTriplets(samples);
        assertTrue(triplets.isEmpty(), "Silent audio should produce no triplets");
    }

    @Test
    void extractTriplets_singleSineTone_producesAtLeastOneTriplet() {
        int    sampleRate = 44_100;
        int    nSamples   = 8192;
        double freq       = 1000.0;

        double[] samples = new double[nSamples];
        for (int i = 0; i < nSamples; i++) {
            samples[i] = Math.sin(2 * Math.PI * freq * i / sampleRate) * 32_000;
        }

        List<Triplet> triplets = TripletAnalyzer.extractTriplets(samples, sampleRate);
        assertFalse(triplets.isEmpty(), "A 1 kHz tone should produce at least one triplet");
    }

    @Test
    void findDominantFrequency_emptyList_returnsZero() {
        assertEquals(0.0, TripletAnalyzer.findDominantFrequency(List.of()));
    }

    @Test
    void findDominantFrequency_returnsFreqOfHighestPeak() {
        Triplet low  = new Triplet(100, 10, 200, 500, 300, 10);
        Triplet high = new Triplet(500, 20, 800, 2000, 900, 15);
        double dom   = TripletAnalyzer.findDominantFrequency(List.of(low, high));
        assertEquals(800.0, dom, 1e-9);
    }

    @Test
    void triplet_amplitudeArray_hasCorrectOrder() {
        Triplet t = new Triplet(1.0, 5.0, 2.0, 100.0, 3.0, 8.0);
        double[] arr = t.amplitudeArray();
        assertEquals(3, arr.length);
        assertEquals(5.0,   arr[0], 1e-9);
        assertEquals(100.0, arr[1], 1e-9);
        assertEquals(8.0,   arr[2], 1e-9);
    }
}
