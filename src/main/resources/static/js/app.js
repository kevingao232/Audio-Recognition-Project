/**
 * AudioFP — browser frontend
 *
 * Recording path:
 *   getUserMedia → ScriptProcessorNode (44 100 Hz mono) → Float32 buffers
 *   → Int16 PCM blob → POST /api/recognize/pcm
 *
 * File-upload path:
 *   FileReader → ArrayBuffer → POST /api/recognize/upload (multipart)
 *
 * Note: ScriptProcessorNode is deprecated in favour of AudioWorklet, but is
 * used here for maximum browser compatibility without a separate worklet file.
 */

const API = '/api';

// ── DOM refs ──────────────────────────────────────────────────────────────────
const recordBtn       = document.getElementById('recordBtn');
const uploadBtn       = document.getElementById('uploadBtn');
const fileInput       = document.getElementById('fileInput');
const statusBar       = document.getElementById('recordingStatus');
const waveformBox     = document.getElementById('waveformBox');
const countdownEl     = document.getElementById('countdown');
const resultCard      = document.getElementById('resultCard');
const songGrid        = document.getElementById('songGrid');
const songCountPill   = document.getElementById('songCount');
const addSongForm     = document.getElementById('addSongForm');
const addStatusEl     = document.getElementById('addStatus');

// ── Recording state ───────────────────────────────────────────────────────────
let isRecording      = false;
let mediaStream      = null;
let audioCtx         = null;
let scriptProcessor  = null;
let recordingBuffers = [];
let countdownTimer   = null;
let secondsLeft      = 10;

const RECORD_SECONDS  = 10;
const SAMPLE_RATE     = 44_100;
const BUFFER_SIZE     = 4096;

// ── Record button ─────────────────────────────────────────────────────────────
recordBtn.addEventListener('click', () => {
  if (isRecording) {
    stopRecording();
  } else {
    startRecording();
  }
});

async function startRecording() {
  try {
    mediaStream = await navigator.mediaDevices.getUserMedia({ audio: true, video: false });
  } catch (err) {
    showStatus(`Microphone access denied: ${err.message}`, 'error');
    return;
  }

  audioCtx        = new AudioContext({ sampleRate: SAMPLE_RATE });
  const source    = audioCtx.createMediaStreamSource(mediaStream);
  scriptProcessor = audioCtx.createScriptProcessor(BUFFER_SIZE, 1, 1);
  recordingBuffers = [];

  scriptProcessor.onaudioprocess = (e) => {
    const data = e.inputBuffer.getChannelData(0);
    recordingBuffers.push(new Float32Array(data));
  };

  source.connect(scriptProcessor);
  scriptProcessor.connect(audioCtx.destination);

  isRecording    = true;
  secondsLeft    = RECORD_SECONDS;
  recordBtn.textContent = '⏹ Stop';
  recordBtn.classList.add('recording');

  hideResult();
  showStatus('Listening… recording for 10 seconds', 'info');
  waveformBox.classList.remove('hidden');
  countdownEl.textContent = secondsLeft;

  countdownTimer = setInterval(() => {
    secondsLeft--;
    countdownEl.textContent = secondsLeft;
    if (secondsLeft <= 0) stopRecording();
  }, 1_000);
}

function stopRecording() {
  if (!isRecording) return;
  clearInterval(countdownTimer);

  scriptProcessor.disconnect();
  mediaStream.getTracks().forEach(t => t.stop());
  audioCtx.close();

  isRecording = false;
  recordBtn.innerHTML = '<span class="btn-icon">🎤</span> Record';
  recordBtn.classList.remove('recording');
  waveformBox.classList.add('hidden');

  const pcmBuffer = mergeToPcm(recordingBuffers);
  showStatus('Analysing audio…', 'info');
  recognizePcm(pcmBuffer);
}

// Merge Float32 buffers → Int16 PCM ArrayBuffer
function mergeToPcm(buffers) {
  const totalLen = buffers.reduce((acc, b) => acc + b.length, 0);
  const merged   = new Float32Array(totalLen);
  let offset = 0;
  for (const buf of buffers) {
    merged.set(buf, offset);
    offset += buf.length;
  }
  const pcm = new Int16Array(merged.length);
  for (let i = 0; i < merged.length; i++) {
    pcm[i] = Math.max(-32_768, Math.min(32_767, Math.round(merged[i] * 32_767)));
  }
  return pcm.buffer;
}

// POST raw PCM to the server
async function recognizePcm(pcmBuffer) {
  try {
    const res  = await fetch(`${API}/recognize/pcm`, {
      method:  'POST',
      headers: { 'Content-Type': 'application/octet-stream' },
      body:    pcmBuffer,
    });
    const data = await res.json();
    clearStatus();
    showResult(data);
  } catch (err) {
    showStatus(`Recognition failed: ${err.message}`, 'error');
  }
}

// ── File upload ───────────────────────────────────────────────────────────────
uploadBtn.addEventListener('click', () => fileInput.click());

fileInput.addEventListener('change', async (e) => {
  const file = e.target.files[0];
  if (!file) return;
  fileInput.value = '';

  hideResult();
  showStatus(`Processing "${file.name}"…`, 'info');

  const formData = new FormData();
  formData.append('file', file);

  try {
    const res  = await fetch(`${API}/recognize/upload`, { method: 'POST', body: formData });
    const data = await res.json();
    clearStatus();
    showResult(data);
  } catch (err) {
    showStatus(`Upload failed: ${err.message}`, 'error');
  }
});

// ── Result rendering ──────────────────────────────────────────────────────────
function showResult(result) {
  resultCard.classList.remove('hidden');

  if (result.recognized) {
    const pct = (result.confidence * 100).toFixed(1);
    resultCard.innerHTML = `
      <div class="result-found">
        <div class="result-icon">🎶</div>
        <div class="result-info">
          <div class="song-name">${esc(result.songName)}</div>
          <div class="artist-name">${esc(result.artist)}</div>
          <span class="confidence">Confidence: ${pct}%</span>
        </div>
      </div>`;
  } else {
    resultCard.innerHTML = `
      <div class="result-not-found">
        <div class="result-icon">❓</div>
        <div class="result-message">${esc(result.message)}</div>
      </div>`;
  }
}

function hideResult() { resultCard.classList.add('hidden'); }

// ── Status helpers ────────────────────────────────────────────────────────────
function showStatus(msg, type) {
  statusBar.textContent = msg;
  statusBar.className   = `status-bar ${type}`;
  statusBar.classList.remove('hidden');
}
function clearStatus() { statusBar.classList.add('hidden'); }

// ── Song library ──────────────────────────────────────────────────────────────
async function loadLibrary() {
  try {
    const res   = await fetch(`${API}/songs`);
    const songs = await res.json();

    songCountPill.textContent = `${songs.length} song${songs.length !== 1 ? 's' : ''}`;

    if (songs.length === 0) {
      songGrid.innerHTML = '<p class="empty-msg">No songs yet — add some below!</p>';
      return;
    }

    songGrid.innerHTML = songs.map(s => `
      <div class="song-card" data-id="${esc(s.id)}">
        <div class="song-info">
          <div class="song-title">${esc(s.name)}</div>
          <div class="song-artist">${esc(s.artist)}</div>
        </div>
        <button class="btn-delete" title="Remove" onclick="deleteSong('${esc(s.id)}')">🗑</button>
      </div>`).join('');
  } catch (err) {
    songGrid.innerHTML = `<p class="empty-msg" style="color:var(--danger)">Failed to load library: ${err.message}</p>`;
  }
}

window.deleteSong = async function(id) {
  if (!confirm('Remove this song from the database?')) return;
  try {
    await fetch(`${API}/songs/${id}`, { method: 'DELETE' });
    loadLibrary();
  } catch (err) {
    alert(`Delete failed: ${err.message}`);
  }
};

// ── Add song form ─────────────────────────────────────────────────────────────
addSongForm.addEventListener('submit', async (e) => {
  e.preventDefault();
  const formData = new FormData(addSongForm);

  setAddStatus('Fingerprinting audio…', 'info');

  try {
    const res = await fetch(`${API}/songs`, { method: 'POST', body: formData });
    if (res.ok) {
      const song = await res.json();
      setAddStatus(`✓ Added "${song.name}" by ${song.artist}`, 'success');
      addSongForm.reset();
      loadLibrary();
    } else {
      setAddStatus('Failed to add song — check server logs.', 'error');
    }
  } catch (err) {
    setAddStatus(`Error: ${err.message}`, 'error');
  }
});

function setAddStatus(msg, type) {
  addStatusEl.textContent = msg;
  addStatusEl.className   = `add-status ${type}`;
  addStatusEl.classList.remove('hidden');
}

// ── Utilities ─────────────────────────────────────────────────────────────────
function esc(str) {
  if (str == null) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

// ── Init ──────────────────────────────────────────────────────────────────────
loadLibrary();
