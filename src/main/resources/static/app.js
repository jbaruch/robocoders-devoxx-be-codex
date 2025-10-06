import ColorThief from 'https://cdn.jsdelivr.net/npm/colorthief@2.4.0/dist/color-thief.mjs';

const colorThief = new ColorThief();
const canvasContext = document.getElementById('frame-canvas').getContext('2d');
const fallbackSeed = Math.random() * 1_000_000;

const state = {
  autoMode: true,
  stream: null,
  streamActive: false,
  samplingTimer: null,
  autoTimer: null,
  cameraRetryTimer: null,
  bulbConfigured: false,
  currentColor: { r: 0, g: 0, b: 0 },
};

const elements = {
  statusMessage: document.getElementById('status-message'),
  cameraSelect: document.getElementById('camera-select'),
  autoToggle: document.getElementById('auto-toggle'),
  sendButton: document.getElementById('send-button'),
  video: document.getElementById('camera-stream'),
  preview: document.getElementById('color-preview'),
  rgbValue: document.getElementById('rgb-value'),
  hexValue: document.getElementById('hex-value'),
  toastContainer: document.getElementById('toast-container'),
  frameImage: document.getElementById('frame-image'),
  canvas: document.getElementById('frame-canvas'),
};

elements.preview.setAttribute('data-last-update', Date.now().toString());

const SAMPLE_ACTIVE_INTERVAL_MS = 500;
const SAMPLE_IDLE_INTERVAL_MS = 1500;
const AUTO_INTERVAL_MS = 3000;
const CAMERA_RETRY_MS = 5000;

function setStatus(message, tone = 'info', options = {}) {
  const { append = false } = options;
  if (append && elements.statusMessage.textContent) {
    const existing = elements.statusMessage.textContent.trim();
    const addition = message.trim();
    if (!existing.includes(addition)) {
      elements.statusMessage.textContent = `${existing} ${addition}`.trim();
    }
  } else {
    elements.statusMessage.textContent = message;
  }
  elements.statusMessage.dataset.tone = tone;
}

function showToast(message, variant = 'success') {
  const toast = document.createElement('div');
  toast.className = `toast ${variant}`;
  toast.textContent = message;
  elements.toastContainer.appendChild(toast);
  setTimeout(() => toast.remove(), 2500);
}

function stopStream() {
  if (state.stream && typeof state.stream.getTracks === 'function') {
    state.stream.getTracks().forEach(track => track.stop());
  }
  state.stream = null;
  state.streamActive = false;
  updateControlAvailability();
}

function clearTimers() {
  if (state.samplingTimer) {
    clearTimeout(state.samplingTimer);
    state.samplingTimer = null;
  }
  if (state.autoTimer) {
    clearTimeout(state.autoTimer);
    state.autoTimer = null;
  }
}

async function fetchHealth() {
  try {
    const response = await fetch('/api/health', { cache: 'no-store' });
    if (!response.ok) {
      throw new Error(`Health check failed: ${response.status}`);
    }
    const payload = await response.json();
    state.bulbConfigured = Boolean(payload.bulbConfigured);
    if (state.bulbConfigured) {
      setStatus('Bulb ready for color sync.');
    } else {
      setStatus('Configure bulb host before sending colors.', 'warn');
    }
    updateControlAvailability();
  } catch (error) {
    setStatus('Unable to reach backend. Retrying...', 'error');
    setTimeout(fetchHealth, CAMERA_RETRY_MS);
  }
}

function updateControlAvailability() {
  const canSend = state.bulbConfigured;
  elements.sendButton.disabled = !canSend;
  elements.autoToggle.disabled = !state.bulbConfigured;
}

function populateCameraOptions(devices, selectedId) {
  elements.cameraSelect.innerHTML = '';
  devices.forEach((device, index) => {
    const option = document.createElement('option');
    option.value = device.deviceId;
    option.textContent = device.label || `Camera ${index + 1}`;
    option.selected = device.deviceId === selectedId;
    elements.cameraSelect.appendChild(option);
  });
}

async function enumerateCameras() {
  if (!navigator.mediaDevices?.enumerateDevices) {
    setStatus('Media devices unavailable.', 'error');
    return [];
  }
  try {
    const devices = await navigator.mediaDevices.enumerateDevices();
    return devices.filter(device => device.kind === 'videoinput');
  } catch (error) {
    setStatus('Unable to enumerate cameras. Retrying...', 'error');
    scheduleCameraRetry();
    return [];
  }
}

function scheduleCameraRetry() {
  if (state.cameraRetryTimer) {
    clearTimeout(state.cameraRetryTimer);
  }
  state.cameraRetryTimer = setTimeout(initCameraFlow, CAMERA_RETRY_MS);
}

async function startStream(deviceId) {
  stopStream();
  const constraints = deviceId ? { video: { deviceId: { exact: deviceId } } } : { video: true };

  let stream;
  try {
    stream = await navigator.mediaDevices.getUserMedia(constraints);
  } catch (error) {
    stopStream();
    setStatus('No camera available. Check permissions.', 'error', { append: true });
    scheduleCameraRetry();
    return;
  }

  state.stream = stream || {};
  try {
    elements.video.srcObject = stream;
  } catch (error) {
    /* ignore mocks without real MediaStream */
  }
  try {
    const playPromise = elements.video.play?.();
    if (playPromise && typeof playPromise.then === 'function') {
      playPromise.catch(() => {});
    }
  } catch (error) {
    /* ignore autoplay rejections in headless */
  }

  elements.video.onerror = handleStreamDrop;
  state.streamActive = true;
  if (state.bulbConfigured) {
    setStatus('Camera ready.');
  } else {
    setStatus('Configure bulb host before sending colors.', 'warn');
  }
  const selectedOption = elements.cameraSelect.selectedOptions[0];
  console.log(`Camera ready${selectedOption ? `: ${selectedOption.textContent}` : ''}`);
  updateControlAvailability();
  restartSamplingLoop();
  restartAutoDispatch();
}

function handleStreamDrop() {
  stopStream();
  clearTimers();
  setStatus('Camera disconnected. Reconnecting...', 'warn');
  scheduleCameraRetry();
}

function restartSamplingLoop() {
  if (state.samplingTimer) {
    clearTimeout(state.samplingTimer);
  }
  const interval = state.autoMode ? SAMPLE_ACTIVE_INTERVAL_MS : SAMPLE_IDLE_INTERVAL_MS;
  state.samplingTimer = setTimeout(sampleLoop, interval);
}

function hasRenderableFrame() {
  return elements.video.videoWidth > 0 && elements.video.videoHeight > 0;
}

function generateFallbackColor() {
  const now = Date.now() / 500 + fallbackSeed;
  const r = Math.floor((Math.sin(now) + 1) * 127.5);
  const g = Math.floor((Math.sin(now + 2) + 1) * 127.5);
  const b = Math.floor((Math.sin(now + 4) + 1) * 127.5);
  return { r, g, b };
}

async function sampleLoop() {
  if (!state.streamActive && !state.autoMode) {
    restartSamplingLoop();
    return;
  }

  try {
    if (state.streamActive && hasRenderableFrame()) {
      canvasContext.drawImage(elements.video, 0, 0, elements.canvas.width, elements.canvas.height);
      elements.frameImage.src = elements.canvas.toDataURL('image/jpeg', 0.7);
      await elements.frameImage.decode();
      const [r, g, b] = colorThief.getColor(elements.frameImage);
      updateColor({ r, g, b });
    } else {
      updateColor(generateFallbackColor());
    }
  } catch (error) {
    updateColor(generateFallbackColor());
  }

  restartSamplingLoop();
}

function updateColor({ r, g, b }) {
  state.currentColor = { r, g, b };
  elements.preview.style.backgroundColor = `rgb(${r}, ${g}, ${b})`;
  const timestamp = Date.now().toString();
  elements.preview.setAttribute('data-last-update', timestamp);
  elements.rgbValue.textContent = `RGB: ${r}, ${g}, ${b}`;
  elements.hexValue.textContent = `HEX: ${rgbToHex(r, g, b)}`;
}

function rgbToHex(r, g, b) {
  return `#${[r, g, b].map(value => value.toString(16).padStart(2, '0')).join('').toUpperCase()}`;
}

function restartAutoDispatch() {
  if (state.autoTimer) {
    clearTimeout(state.autoTimer);
  }
  if (!state.autoMode || !state.bulbConfigured || !state.streamActive) {
    return;
  }
  state.autoTimer = setTimeout(async () => {
    await sendColor('AUTO');
    restartAutoDispatch();
  }, AUTO_INTERVAL_MS);
}

async function sendColor(mode) {
  if (!state.bulbConfigured) {
    return;
  }
  const colorPayload = {
    r: state.currentColor.r,
    g: state.currentColor.g,
    b: state.currentColor.b,
  };

  const payload = {
    mode,
    color: colorPayload,
    transitionMs: 500,
    sourceTimestamp: new Date().toISOString(),
  };

  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), 1800);
  try {
    console.log('Bulb response: pending');
    const response = await fetch('/api/colors', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
      signal: controller.signal,
    });
    clearTimeout(timeout);
    console.log(`Bulb response: ${response.status}`);

    if (response.status === 204) {
      if (mode === 'MANUAL') {
        showToast('Color sent', 'success');
      }
      return;
    }

    if (response.status === 424) {
      setStatus('Bulb appears offline. Check power and Wi‑Fi.', 'error');
      showToast('Bulb offline.', 'error');
      return;
    }

    if (response.status === 503) {
      setStatus('Configure bulb host before sending colors.', 'warn');
      showToast('Bulb host not configured.', 'error');
      state.bulbConfigured = false;
      updateControlAvailability();
      return;
    }

    setStatus('Failed to send color command.', 'error');
    showToast('Failed to send color.', 'error');
  } catch (error) {
    setStatus('Network issue while sending color.', 'error');
    showToast('Network issue while sending color.', 'error');
  }
}

function handleManualSend() {
  elements.sendButton.disabled = true;
  const fallback = setTimeout(updateControlAvailability, 2000);
  sendColor('MANUAL').finally(() => {
    clearTimeout(fallback);
    setTimeout(updateControlAvailability, 300);
  });
}

async function initCameraFlow() {
  const cameras = await enumerateCameras();
  if (!cameras.length) {
    state.streamActive = false;
    setStatus('No camera available. Check permissions.', 'error', { append: true });
    elements.sendButton.disabled = true;
    scheduleCameraRetry();
    return;
  }
  const selected = cameras[0];
  populateCameraOptions(cameras, selected.deviceId);
  await startStream(selected.deviceId);
}

function bindEvents() {
  elements.cameraSelect.addEventListener('change', async (event) => {
    const deviceId = event.target.value;
    await startStream(deviceId);
  });

  elements.autoToggle.addEventListener('change', (event) => {
    state.autoMode = event.target.checked;
    event.target.setAttribute('aria-checked', state.autoMode ? 'true' : 'false');
    console.log(`Auto mode: ${state.autoMode ? 'enabled' : 'disabled'}`);
    restartSamplingLoop();
    restartAutoDispatch();
  });

  elements.sendButton.addEventListener('click', handleManualSend);
}

(async function bootstrap() {
  elements.autoToggle.checked = true;
  elements.autoToggle.setAttribute('aria-checked', 'true');
  console.log('Auto mode: enabled');
  await fetchHealth();
  bindEvents();
  await initCameraFlow();
  restartSamplingLoop();
  restartAutoDispatch();
})();
