# AudioFP — Audio Fingerprint Recognition

A Shazam-style audio recognition system built on FFT-based spectral fingerprinting. Originally a high-school Java project; now a fully modernised Spring Boot web application.

---

## How It Works

1. **Capture** — Record 10 seconds of audio from the microphone (browser or server-side) or upload a WAV file.
2. **Window** — Apply a Hann window to the samples to reduce spectral leakage.
3. **FFT** — Run a radix-2 Cooley-Tukey FFT. The input is zero-padded to the next power of 2.
4. **Triplets** — Scan the 20–5 000 Hz range of the magnitude spectrum for *trough → peak → trough* landmarks above 30 % of the max amplitude. Each such pattern is a **triplet**.
5. **Fingerprint** — Store the dominant frequency and the full triplet list as JSON.
6. **Match** — Compare a recorded fingerprint against every stored song using Pearson correlation of the amplitude arrays at matching frequency positions. The best-scoring song above the 0.60 threshold is returned.

---

## Quick Start

### Prerequisites

| Tool | Version |
|------|---------|
| JDK  | 17+     |
| Maven| 3.9+    |

### Build & Run

```bash
# From the project root
mvn spring-boot:run
```

Then open **http://localhost:8080** in your browser.

### Build a fat JAR

```bash
mvn package
java -jar target/audio-fingerprint-1.0.0.jar
```

---

## Web Interface

| Section | Description |
|---------|-------------|
| **Identify** | Record from the browser microphone (10 s) or upload a WAV/AIFF file for instant recognition |
| **Library** | Browse and delete songs in the fingerprint database |
| **Add Song** | Upload a WAV file with a name and artist to fingerprint and store it |

---

## REST API

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/recognize/upload` | Multipart WAV upload → recognition result |
| `POST` | `/api/recognize/pcm` | Raw 16-bit LE PCM body → recognition result |
| `POST` | `/api/recognize/microphone` | Record from server microphone → recognition result |
| `GET`  | `/api/songs` | List all songs in the database |
| `POST` | `/api/songs` | Add a song (multipart: `name`, `artist`, `file`) |
| `DELETE` | `/api/songs/{id}` | Remove a song by UUID |

### Example — add a song

```bash
curl -X POST http://localhost:8080/api/songs \
  -F "name=Bohemian Rhapsody" \
  -F "artist=Queen" \
  -F "file=@bohemian_rhapsody.wav"
```

### Example — recognise a file

```bash
curl -X POST http://localhost:8080/api/recognize/upload \
  -F "file=@clip.wav"
```

---

## Configuration

Edit `src/main/resources/application.properties`:

```properties
server.port=8080
audiofp.database.path=fingerprints.json   # path to the JSON database file
spring.servlet.multipart.max-file-size=100MB
```

---

## Running Tests

```bash
mvn test
```

---

## Project Structure

```
src/
├── main/
│   ├── java/com/audiofp/
│   │   ├── AudioFingerprintApplication.java   # Spring Boot entry point
│   │   ├── audio/
│   │   │   ├── Complex.java                   # Immutable complex number
│   │   │   ├── FFT.java                       # Radix-2 FFT + Hann window
│   │   │   ├── AudioCapture.java              # Mic recording + WAV decoding
│   │   │   └── TripletAnalyzer.java           # Spectral triplet extraction
│   │   ├── matching/
│   │   │   └── Correlation.java               # Pearson correlation matching
│   │   ├── model/
│   │   │   ├── Song.java
│   │   │   ├── Fingerprint.java
│   │   │   ├── TripletData.java
│   │   │   └── RecognitionResult.java
│   │   ├── database/
│   │   │   └── FingerprintDatabase.java       # JSON-backed in-memory store
│   │   ├── service/
│   │   │   └── RecognitionService.java        # Recognition pipeline
│   │   └── controller/
│   │       └── RecognitionController.java     # REST endpoints
│   └── resources/
│       ├── application.properties
│       └── static/                            # Web frontend
│           ├── index.html
│           ├── css/style.css
│           └── js/app.js
├── test/
│   └── java/com/audiofp/
│       ├── audio/FFTTest.java
│       ├── audio/TripletAnalyzerTest.java
│       └── matching/CorrelationTest.java
└── legacy/                                    # Original high-school source files
```

---

## What Changed from the Original

| Issue | Original | Fixed |
|-------|----------|-------|
| Deprecated internal APIs | `com.sun.*`, `sun.audio.*`, `org.omg.CORBA` | Removed entirely |
| No build system | Raw `.java` files | Maven + Spring Boot |
| Missing `TripletAnalyzer` | Empty class | Fully implemented |
| `Semblance.java` bugs | `recording = data` overwrote field; equality check inverted | Not carried forward (semblance superseded by Pearson correlation) |
| `Correlation` outer-loop bug | `x += 2` over a 2-row array | Rewritten with clear per-triplet matching |
| No error handling | Raw checked exceptions propagated | Proper HTTP error responses + logging |
| Hard-coded Unix paths | `/Users/Kevin/Documents/…` | Configurable via `application.properties` |
| No frontend | CLI only | Full web UI with mic recording & file upload |
