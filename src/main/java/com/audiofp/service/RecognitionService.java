package com.audiofp.service;

import com.audiofp.audio.AudioCapture;
import com.audiofp.audio.TripletAnalyzer;
import com.audiofp.audio.TripletAnalyzer.Triplet;
import com.audiofp.database.FingerprintDatabase;
import com.audiofp.matching.Correlation;
import com.audiofp.model.Fingerprint;
import com.audiofp.model.RecognitionResult;
import com.audiofp.model.Song;
import com.audiofp.model.TripletData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.List;

/**
 * Core recognition and fingerprint-building logic.
 *
 * The recognition pipeline is:
 * <ol>
 *   <li>Decode input audio bytes → double[] samples</li>
 *   <li>FFT + Hann window → magnitude spectrum</li>
 *   <li>Extract spectral triplets</li>
 *   <li>Find dominant frequency; narrow candidate list by frequency proximity</li>
 *   <li>Run Pearson correlation against each candidate's stored fingerprint</li>
 *   <li>Return the best-scoring song if score ≥ {@value #CONFIDENCE_THRESHOLD}</li>
 * </ol>
 */
@Service
public class RecognitionService {

    private static final Logger log = LoggerFactory.getLogger(RecognitionService.class);

    /** Minimum correlation score accepted as a confirmed match. */
    private static final double CONFIDENCE_THRESHOLD = 0.60;

    /** Frequency window (Hz) used to pre-filter database candidates. */
    private static final double FREQ_TOLERANCE = 50.0;

    private final FingerprintDatabase database;

    public RecognitionService(FingerprintDatabase database) {
        this.database = database;
    }

    // -------------------------------------------------------------------------
    // Recognition
    // -------------------------------------------------------------------------

    /**
     * Recognises audio from raw bytes (WAV file or 16-bit LE PCM).
     */
    public RecognitionResult recognize(byte[] audioBytes)
            throws IOException, UnsupportedAudioFileException {
        double[] samples = AudioCapture.readAudioBytes(audioBytes);
        return recognizeFromSamples(samples);
    }

    /**
     * Records from the system microphone, then recognises.
     */
    public RecognitionResult recognizeFromMicrophone() throws LineUnavailableException {
        double[] samples = AudioCapture.recordFromMicrophone();
        return recognizeFromSamples(samples);
    }

    /**
     * Core recognition logic operating on a pre-decoded sample array.
     */
    public RecognitionResult recognizeFromSamples(double[] samples) {
        List<Triplet> triplets = TripletAnalyzer.extractTriplets(samples);

        if (triplets.isEmpty()) {
            return RecognitionResult.error(
                    "No spectral features detected — is there sound playing?");
        }

        double dominantFreq = TripletAnalyzer.findDominantFrequency(triplets);
        log.info("Querying with dominant freq {} Hz, {} triplets", dominantFreq, triplets.size());

        // Narrow the search space; fall back to all songs if no nearby match exists
        List<Song> candidates = database.findCandidatesByFrequency(dominantFreq, FREQ_TOLERANCE);
        if (candidates.isEmpty()) {
            log.debug("No frequency-filtered candidates; searching all {} song(s).",
                    database.getAllSongs().size());
            candidates = database.getAllSongs();
        }

        Song   bestSong  = null;
        double bestScore = CONFIDENCE_THRESHOLD;

        for (Song candidate : candidates) {
            if (candidate.getFingerprint() == null) continue;

            List<Triplet> dbTriplets = candidate.getFingerprint().toTripletList();
            double score = Correlation.computeMatchScore(triplets, dbTriplets);
            log.debug("Candidate '{}' by '{}': score={}", candidate.getName(), candidate.getArtist(), score);

            if (score > bestScore) {
                bestScore = score;
                bestSong  = candidate;
            }
        }

        if (bestSong != null) {
            log.info("Match found: '{}' (confidence={})", bestSong.getName(), bestScore);
            return RecognitionResult.found(bestSong, bestScore);
        }

        log.info("No match found above threshold {}.", CONFIDENCE_THRESHOLD);
        return RecognitionResult.notFound();
    }

    // -------------------------------------------------------------------------
    // Database population
    // -------------------------------------------------------------------------

    /**
     * Builds a fingerprint from raw audio bytes and stores the song.
     *
     * @param name      song title
     * @param artist    artist name
     * @param duration  duration in seconds (0 if unknown)
     * @param audioBytes WAV file bytes or raw 16-bit LE PCM
     */
    public Song buildAndStoreSong(String name, String artist, double duration, byte[] audioBytes)
            throws IOException, UnsupportedAudioFileException {
        double[] samples = AudioCapture.readAudioBytes(audioBytes);
        return buildAndStoreSong(name, artist, duration, samples);
    }

    public Song buildAndStoreSong(String name, String artist, double duration, double[] samples) {
        List<Triplet> triplets    = TripletAnalyzer.extractTriplets(samples);
        double dominantFreq       = TripletAnalyzer.findDominantFrequency(triplets);
        double maxAmp             = triplets.stream().mapToDouble(Triplet::peakAmp).max().orElse(0);
        List<TripletData> tdList  = triplets.stream().map(TripletData::from).toList();

        Fingerprint fp = new Fingerprint(dominantFreq, maxAmp, tdList);
        Song song      = new Song(name, artist, duration, fp);
        database.addSong(song);

        log.info("Stored '{}' by '{}' — dominantFreq={} Hz, {} triplets.",
                name, artist, dominantFreq, triplets.size());
        return song;
    }
}
