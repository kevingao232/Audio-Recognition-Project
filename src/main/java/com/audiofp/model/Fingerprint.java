package com.audiofp.model;

import com.audiofp.audio.TripletAnalyzer.Triplet;

import java.util.Collections;
import java.util.List;

/**
 * The spectral fingerprint of a song: dominant frequency, max amplitude, and
 * the list of trough-peak-trough triplets extracted from the full-song FFT.
 */
public class Fingerprint {

    private double dominantFreq;
    private double maxAmplitude;
    private List<TripletData> triplets;

    /** Required by Jackson for deserialisation. */
    public Fingerprint() {}

    public Fingerprint(double dominantFreq, double maxAmplitude, List<TripletData> triplets) {
        this.dominantFreq = dominantFreq;
        this.maxAmplitude = maxAmplitude;
        this.triplets     = triplets;
    }

    // -------------------------------------------------------------------------
    // Conversion
    // -------------------------------------------------------------------------

    /** Converts the stored {@link TripletData} records back to {@link Triplet} objects. */
    public List<Triplet> toTripletList() {
        if (triplets == null) return Collections.emptyList();
        return triplets.stream().map(TripletData::toTriplet).toList();
    }

    // -------------------------------------------------------------------------
    // Getters / setters (required for Jackson)
    // -------------------------------------------------------------------------

    public double getDominantFreq()                { return dominantFreq; }
    public void   setDominantFreq(double v)        { this.dominantFreq = v; }

    public double getMaxAmplitude()                { return maxAmplitude; }
    public void   setMaxAmplitude(double v)        { this.maxAmplitude = v; }

    public List<TripletData> getTriplets()         { return triplets; }
    public void   setTriplets(List<TripletData> v) { this.triplets = v; }
}
