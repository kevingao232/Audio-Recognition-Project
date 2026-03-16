package com.audiofp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the AudioFP Spring Boot application.
 *
 * Once running, open http://localhost:8080 in your browser to use the
 * web interface, or call the REST API directly (see RecognitionController).
 */
@SpringBootApplication
public class AudioFingerprintApplication {

    public static void main(String[] args) {
        SpringApplication.run(AudioFingerprintApplication.class, args);
    }
}
