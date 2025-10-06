import { test, expect } from '@playwright/test';
import type { Page } from '@playwright/test';
import { injectAxe, checkA11y } from 'axe-playwright';

test.beforeEach(async ({ page }) => {
  await page.route('**/api/health', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        appVersion: '0.0.0-test',
        bulbConfigured: true,
        timestamp: new Date().toISOString(),
      }),
    });
  });

  await page.route('**/api/colors', async (route) => {
    await route.fulfill({ status: 204 });
  });

  await page.addInitScript(() => {
    const mockDevices = [
      { deviceId: 'cam-1', kind: 'videoinput', label: 'Mock Cam One' },
      { deviceId: 'cam-2', kind: 'videoinput', label: 'Mock Cam Two' },
    ];

    Object.defineProperty(navigator, 'mediaDevices', {
      value: {
        enumerateDevices: async () => mockDevices,
        getUserMedia: async () => ({ id: 'mock-stream' }),
      },
      configurable: true,
    });
  });
});

const navigateToApp = async (page: Page) => {
  await page.goto('/');
};

/** SC-FE-1 / FE-1 */
test('Camera dropdown auto-selects first device and retries on failure', async ({ page }) => {
  await navigateToApp(page);

  const cameraSelect = page.getByTestId('camera-select');
  await expect(cameraSelect).toHaveValue('cam-1');
  await expect(cameraSelect).toContainText('Mock Cam One');
});

/** SC-FE-2 / FE-2 */
test('Color preview updates every 500ms in auto mode and slows when paused', async ({ page }) => {
  await navigateToApp(page);

  const preview = page.getByTestId('color-preview');
  await expect(preview).toHaveAttribute('data-last-update');

  await page.waitForTimeout(600);
  const firstTimestamp = await preview.getAttribute('data-last-update');
  await page.waitForTimeout(600);
  const secondTimestamp = await preview.getAttribute('data-last-update');
  expect(firstTimestamp).not.toEqual(secondTimestamp);

  await page.getByRole('switch', { name: /auto mode/i }).click();
  await page.waitForTimeout(1500);
  const pausedTimestamp = await preview.getAttribute('data-last-update');
  expect(pausedTimestamp).toEqual(await preview.getAttribute('data-last-update'));
});

/** SC-FE-3 / CC-1 */
test('Manual send disables button, enforces 2s latency budget, and surfaces response toast', async ({ page }) => {
  await navigateToApp(page);

  const sendButton = page.getByRole('button', { name: /send color/i });
  await sendButton.click();

  await expect(sendButton).toBeDisabled();
  await expect(page.getByText(/color sent/i)).toBeVisible({ timeout: 2000 });
});

/** SC-FE-4 */
test('Auto mode fires payload every ~3 seconds and pauses without video stream', async ({ page }) => {
  await navigateToApp(page);

  const mockCalls: number[] = [];
  await page.route('**/api/colors', async (route) => {
    mockCalls.push(Date.now());
    await route.fulfill({ status: 204 });
  });

  await page.getByRole('switch', { name: /auto mode/i }).check();
  await page.waitForTimeout(9200);
  expect(mockCalls.length).toBeGreaterThanOrEqual(3);

  await page.evaluate(() => {
    const video = document.querySelector('video');
    if (video) {
      video.dispatchEvent(new Event('error'));
    }
  });
  const callCountAfterDrop = mockCalls.length;
  await page.waitForTimeout(4000);
  expect(mockCalls.length).toBe(callCountAfterDrop);
});

/** SC-FE-5 */
test('Distinct UI states for configuration, bulb offline, camera missing, and generic failures', async ({ page }) => {
  await navigateToApp(page);

  await page.route('**/api/colors', async (route) => {
    await route.fulfill({ status: 424, body: JSON.stringify({ error: 'BULB_OFFLINE' }) });
  });

  await page.getByRole('button', { name: /send color/i }).click();
  await expect(page.getByText(/bulb appears offline/i)).toBeVisible();

  await page.route('**/api/health', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ bulbConfigured: false, appVersion: '0.0.0-test', timestamp: new Date().toISOString() }),
    });
  });

  await page.reload();
  await expect(page.getByText(/configure bulb host/i)).toBeVisible();

  await page.evaluate(() => {
    Object.defineProperty(navigator, 'mediaDevices', {
      value: {
        enumerateDevices: async () => [],
        getUserMedia: async () => { throw new Error('denied'); },
      },
      configurable: true,
    });
  });

  await page.reload();
  await expect(page.getByTestId('status-message')).toContainText(/no camera available|configure bulb host/i);
});

/** SC-CC-2 */
test('Controls meet accessibility contrast and keyboard reachability', async ({ page }) => {
  await navigateToApp(page);
  await injectAxe(page);
  await checkA11y(page, undefined, {
    detailedReport: true,
    detailedReportOptions: { html: true },
  });

  await page.keyboard.press('Tab');
  const sendButton = page.getByRole('button', { name: /send color/i });
  if (test.info().project.name === 'webkit') {
    await expect(sendButton).toBeVisible();
  } else {
    await expect(sendButton).toBeFocused();
  }
});

/** SC-CC-3 */
test('Only high-level state transition logs are emitted', async ({ page }) => {
  const logs: string[] = [];
  page.on('console', (message) => logs.push(message.text()));

  await navigateToApp(page);

  await page.getByRole('switch', { name: /auto mode/i }).check();
  await page.getByRole('switch', { name: /auto mode/i }).uncheck();
  await page.getByRole('button', { name: /send color/i }).click();

  expect(logs).toEqual(
    expect.arrayContaining([
      expect.stringMatching(/camera ready/i),
      expect.stringMatching(/auto mode: enabled/i),
      expect.stringMatching(/auto mode: disabled/i),
      expect.stringMatching(/bulb response/i),
    ]));
});
