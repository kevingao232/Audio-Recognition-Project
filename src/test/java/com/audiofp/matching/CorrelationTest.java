package com.audiofp.matching;

import com.audiofp.audio.TripletAnalyzer.Triplet;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CorrelationTest {

    // ── pearsonCorrelation() ──────────────────────────────────────────────────

    @Test
    void pearsonCorrelation_identicalArrays_returnsOne() {
        double[] x = {1, 2, 3, 4, 5};
        double r   = Correlation.pearsonCorrelation(x, x.clone());
        assertEquals(1.0, r, 1e-9);
    }

    @Test
    void pearsonCorrelation_perfectlyAntiCorrelated_returnsMinusOne() {
        double[] x = {1, 2, 3, 4, 5};
        double[] y = {5, 4, 3, 2, 1};
        double r   = Correlation.pearsonCorrelation(x, y);
        assertEquals(-1.0, r, 1e-9);
    }

    @Test
    void pearsonCorrelation_constantArray_returnsNaN() {
        double[] x = {3, 3, 3};
        double[] y = {1, 2, 3};
        assertTrue(Double.isNaN(Correlation.pearsonCorrelation(x, y)));
    }

    @Test
    void pearsonCorrelation_nullInput_returnsNaN() {
        assertTrue(Double.isNaN(Correlation.pearsonCorrelation(null, new double[]{1, 2})));
    }

    @Test
    void pearsonCorrelation_differentLengths_returnsNaN() {
        double[] x = {1, 2, 3};
        double[] y = {1, 2};
        assertTrue(Double.isNaN(Correlation.pearsonCorrelation(x, y)));
    }

    // ── computeMatchScore() ───────────────────────────────────────────────────

    @Test
    void computeMatchScore_emptyRecorded_returnsMinusOne() {
        Triplet t = new Triplet(100, 10, 200, 100, 300, 10);
        assertEquals(-1.0, Correlation.computeMatchScore(List.of(), List.of(t)));
    }

    @Test
    void computeMatchScore_emptyStored_returnsMinusOne() {
        Triplet t = new Triplet(100, 10, 200, 100, 300, 10);
        assertEquals(-1.0, Correlation.computeMatchScore(List.of(t), List.of()));
    }

    @Test
    void computeMatchScore_identicalFingerprints_returnsHighScore() {
        // Recorded and stored are the same — should correlate at 1.0
        Triplet t1 = new Triplet(50, 30, 440, 500, 600, 20);
        Triplet t2 = new Triplet(700, 15, 880, 300, 1000, 10);

        List<Triplet> fp = List.of(t1, t2);
        double score = Correlation.computeMatchScore(fp, fp);

        assertTrue(score > 0.9,
                "Identical fingerprints should score > 0.9, got: " + score);
    }

    @Test
    void computeMatchScore_veryDifferentFrequencies_returnsMinusOne() {
        // Peaks are far apart → no frequency match → −1
        Triplet recorded = new Triplet(50,  10, 440,  100, 600,  10);
        Triplet stored   = new Triplet(500, 10, 4000, 100, 4200, 10);

        double score = Correlation.computeMatchScore(
                List.of(recorded), List.of(stored));
        assertEquals(-1.0, score);
    }
}
