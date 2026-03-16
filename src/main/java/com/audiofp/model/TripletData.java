package com.audiofp.model;

import com.audiofp.audio.TripletAnalyzer.Triplet;

/**
 * JSON-serialisable representation of a {@link Triplet}.
 * Stored in {@link Fingerprint} and persisted to the database file.
 */
public record TripletData(
        double troughFreq1,
        double troughAmp1,
        double peakFreq,
        double peakAmp,
        double troughFreq2,
        double troughAmp2) {

    public static TripletData from(Triplet t) {
        return new TripletData(
                t.troughFreq1(), t.troughAmp1(),
                t.peakFreq(),    t.peakAmp(),
                t.troughFreq2(), t.troughAmp2());
    }

    public Triplet toTriplet() {
        return new Triplet(troughFreq1, troughAmp1,
                           peakFreq,    peakAmp,
                           troughFreq2, troughAmp2);
    }
}
