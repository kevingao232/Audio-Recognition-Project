package com.audiofp.database;

import com.audiofp.model.Song;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * In-memory song database backed by a JSON file on disk.
 *
 * The database is loaded once on startup and persisted to disk on every
 * write. This is appropriate for a single-node application; for multi-node
 * deployments a proper data store should be used instead.
 */
@Component
public class FingerprintDatabase {

    private static final Logger log = LoggerFactory.getLogger(FingerprintDatabase.class);

    private final ObjectMapper     mapper = new ObjectMapper();
    private final List<Song>       songs  = new ArrayList<>();

    @Value("${audiofp.database.path:fingerprints.json}")
    private String dbPath;

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    @PostConstruct
    public void load() {
        File file = new File(dbPath);
        if (!file.exists()) {
            log.info("No existing database at '{}' — starting with an empty library.", dbPath);
            return;
        }
        try {
            Song[] loaded = mapper.readValue(file, Song[].class);
            songs.addAll(List.of(loaded));
            log.info("Loaded {} song(s) from '{}'.", songs.size(), dbPath);
        } catch (IOException e) {
            log.error("Failed to load database from '{}': {}", dbPath, e.getMessage());
        }
    }

    public void save() {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(dbPath), songs);
            log.info("Database saved — {} song(s) in '{}'.", songs.size(), dbPath);
        } catch (IOException e) {
            log.error("Failed to save database to '{}': {}", dbPath, e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // CRUD
    // -------------------------------------------------------------------------

    public synchronized void addSong(Song song) {
        songs.removeIf(s -> s.getId().equals(song.getId()));
        songs.add(song);
        save();
    }

    public synchronized boolean removeSong(String id) {
        boolean removed = songs.removeIf(s -> s.getId().equals(id));
        if (removed) save();
        return removed;
    }

    public Optional<Song> findById(String id) {
        return songs.stream().filter(s -> s.getId().equals(id)).findFirst();
    }

    public List<Song> getAllSongs() {
        return Collections.unmodifiableList(songs);
    }

    /**
     * Returns songs whose dominant frequency is within {@code toleranceHz} of
     * {@code targetFreq}. Used to narrow the search space before running the
     * more expensive full correlation.
     */
    public List<Song> findCandidatesByFrequency(double targetFreq, double toleranceHz) {
        return songs.stream()
                .filter(s -> s.getFingerprint() != null
                        && Math.abs(s.getFingerprint().getDominantFreq() - targetFreq) <= toleranceHz)
                .toList();
    }
}
