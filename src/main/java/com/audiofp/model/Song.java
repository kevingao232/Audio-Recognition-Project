package com.audiofp.model;

import java.util.UUID;

/**
 * Song metadata + fingerprint stored in the database.
 */
public class Song {

    private String      id;
    private String      name;
    private String      artist;
    private double      duration;   // seconds
    private Fingerprint fingerprint;

    /** Required by Jackson. */
    public Song() {
        this.id = UUID.randomUUID().toString();
    }

    public Song(String name, String artist, double duration, Fingerprint fingerprint) {
        this.id          = UUID.randomUUID().toString();
        this.name        = name;
        this.artist      = artist;
        this.duration    = duration;
        this.fingerprint = fingerprint;
    }

    // Getters / setters -------------------------------------------------------

    public String      getId()                     { return id; }
    public void        setId(String id)            { this.id = id; }

    public String      getName()                   { return name; }
    public void        setName(String name)        { this.name = name; }

    public String      getArtist()                 { return artist; }
    public void        setArtist(String artist)    { this.artist = artist; }

    public double      getDuration()               { return duration; }
    public void        setDuration(double d)       { this.duration = d; }

    public Fingerprint getFingerprint()            { return fingerprint; }
    public void        setFingerprint(Fingerprint fp) { this.fingerprint = fp; }
}
