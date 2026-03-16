package com.audiofp.model;

/**
 * API response returned after an audio recognition attempt.
 */
public class RecognitionResult {

    private boolean recognized;
    private String  songId;
    private String  songName;
    private String  artist;
    private double  confidence;
    private String  message;

    // -------------------------------------------------------------------------
    // Static factories
    // -------------------------------------------------------------------------

    public static RecognitionResult found(Song song, double confidence) {
        RecognitionResult r = new RecognitionResult();
        r.recognized = true;
        r.songId     = song.getId();
        r.songName   = song.getName();
        r.artist     = song.getArtist();
        r.confidence = confidence;
        r.message    = "Song recognised successfully.";
        return r;
    }

    public static RecognitionResult notFound() {
        RecognitionResult r = new RecognitionResult();
        r.recognized = false;
        r.message    = "No matching song found in the database.";
        return r;
    }

    public static RecognitionResult error(String msg) {
        RecognitionResult r = new RecognitionResult();
        r.recognized = false;
        r.message    = msg;
        return r;
    }

    // -------------------------------------------------------------------------
    // Getters (no setters needed — created via factories)
    // -------------------------------------------------------------------------

    public boolean isRecognized()  { return recognized; }
    public String  getSongId()     { return songId; }
    public String  getSongName()   { return songName; }
    public String  getArtist()     { return artist; }
    public double  getConfidence() { return confidence; }
    public String  getMessage()    { return message; }
}
