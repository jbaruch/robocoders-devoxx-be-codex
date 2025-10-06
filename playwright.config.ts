import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  webServer: {
    command: 'npx http-server ./src/main/resources/static --port 4173 --silent',
    port: 4173,
    reuseExistingServer: true,
    timeout: 120_000,
  },
  testDir: './tests',
  timeout: 30_000,
  expect: {
    timeout: 5_000,
  },
  use: {
    baseURL: 'http://127.0.0.1:4173',
    trace: 'retain-on-failure',
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
    {
      name: 'firefox',
      use: { ...devices['Desktop Firefox'] },
    },
    {
      name: 'webkit',
      use: { ...devices['Desktop Safari'] },
    },
  ],
});
