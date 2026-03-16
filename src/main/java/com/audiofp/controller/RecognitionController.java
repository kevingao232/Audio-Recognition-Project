package com.audiofp.controller;

import com.audiofp.database.FingerprintDatabase;
import com.audiofp.model.RecognitionResult;
import com.audiofp.model.Song;
import com.audiofp.service.RecognitionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * REST API for audio recognition and database management.
 *
 * <pre>
 * POST  /api/recognize/upload      — recognise from a WAV/PCM file upload
 * POST  /api/recognize/pcm         — recognise from raw 16-bit LE PCM bytes
 * POST  /api/recognize/microphone  — record from server microphone + recognise
 *
 * GET    /api/songs                — list all songs in the database
 * POST   /api/songs                — add a song (multipart: name, artist, file)
 * DELETE /api/songs/{id}           — remove a song by ID
 * </pre>
 */
@RestController
@RequestMapping("/api")
public class RecognitionController {

    private static final Logger log = LoggerFactory.getLogger(RecognitionController.class);

    private final RecognitionService  recognitionService;
    private final FingerprintDatabase database;

    public RecognitionController(RecognitionService recognitionService,
                                 FingerprintDatabase database) {
        this.recognitionService = recognitionService;
        this.database           = database;
    }

    // -------------------------------------------------------------------------
    // Recognition endpoints
    // -------------------------------------------------------------------------

    /**
     * Recognise audio from a WAV file upload (multipart/form-data).
     * The browser Web Audio API recording is sent here as a WAV blob.
     */
    @PostMapping("/recognize/upload")
    public ResponseEntity<RecognitionResult> recognizeUpload(
            @RequestParam("file") MultipartFile file) {
        try {
            RecognitionResult result = recognitionService.recognize(file.getBytes());
            return ResponseEntity.ok(result);
        } catch (UnsupportedAudioFileException e) {
            log.warn("Unsupported audio format in recognition upload", e);
            return ResponseEntity.badRequest()
                    .body(RecognitionResult.error("Unsupported audio format: " + e.getMessage()));
        } catch (IOException e) {
            log.error("IO error during recognition upload", e);
            return ResponseEntity.internalServerError()
                    .body(RecognitionResult.error("Failed to read file: " + e.getMessage()));
        }
    }

    /**
     * Recognise from raw 16-bit signed little-endian PCM bytes sent as the
     * request body (Content-Type: application/octet-stream).
     * Used by the browser frontend's mic-recording path.
     */
    @PostMapping(value = "/recognize/pcm", consumes = "application/octet-stream")
    public ResponseEntity<RecognitionResult> recognizePcm(@RequestBody byte[] pcmBytes) {
        try {
            RecognitionResult result = recognitionService.recognize(pcmBytes);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error during PCM recognition", e);
            return ResponseEntity.internalServerError()
                    .body(RecognitionResult.error(e.getMessage()));
        }
    }

    /**
     * Record from the server's own microphone and recognise.
     * Useful when running locally; won't work on a headless server.
     */
    @PostMapping("/recognize/microphone")
    public ResponseEntity<RecognitionResult> recognizeMicrophone() {
        try {
            RecognitionResult result = recognitionService.recognizeFromMicrophone();
            return ResponseEntity.ok(result);
        } catch (LineUnavailableException e) {
            log.error("Microphone unavailable", e);
            return ResponseEntity.internalServerError()
                    .body(RecognitionResult.error("Microphone not available: " + e.getMessage()));
        }
    }

    // -------------------------------------------------------------------------
    // Library endpoints
    // -------------------------------------------------------------------------

    /** Returns all songs currently stored in the database. */
    @GetMapping("/songs")
    public ResponseEntity<List<Song>> listSongs() {
        return ResponseEntity.ok(database.getAllSongs());
    }

    /**
     * Fingerprints an audio file and adds it to the database.
     *
     * @param name     song title
     * @param artist   artist name
     * @param duration optional duration in seconds (defaults to 0)
     * @param file     WAV audio file
     */
    @PostMapping("/songs")
    public ResponseEntity<Song> addSong(
            @RequestParam String name,
            @RequestParam String artist,
            @RequestParam(defaultValue = "0") double duration,
            @RequestParam("file") MultipartFile file) {
        try {
            Song song = recognitionService.buildAndStoreSong(
                    name, artist, duration, file.getBytes());
            return ResponseEntity.ok(song);
        } catch (UnsupportedAudioFileException e) {
            log.warn("Unsupported format when adding song '{}'", name, e);
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            log.error("IO error when adding song '{}'", name, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /** Removes a song from the database by its UUID. */
    @DeleteMapping("/songs/{id}")
    public ResponseEntity<Map<String, Boolean>> deleteSong(@PathVariable String id) {
        boolean removed = database.removeSong(id);
        return ResponseEntity.ok(Map.of("removed", removed));
    }
}
