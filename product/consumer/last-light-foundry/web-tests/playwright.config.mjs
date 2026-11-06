import { defineConfig } from '@playwright/test';

// The end-to-end tests drive a real browser against the real Java server. Set LLF_BASE_URL to use
// an already running server; otherwise Playwright starts one with the documented Gradle task.
const external = process.env.LLF_BASE_URL;
const port = process.env.LLF_PORT ?? '8551';

export default defineConfig({
  testDir: './e2e',
  workers: 1,
  fullyParallel: false,
  retries: 0,
  timeout: 60_000,
  reporter: [['list']],
  use: {
    baseURL: external ?? `http://127.0.0.1:${port}`,
    viewport: { width: 1366, height: 800 },
    trace: 'retain-on-failure',
  },
  webServer: external
    ? undefined
    : {
        command: './gradlew --no-daemon :last-light-foundry:run',
        cwd: '../../..',
        env: { LLF_PORT: port },
        url: `http://127.0.0.1:${port}/api/missions`,
        timeout: 300_000,
        reuseExistingServer: false,
      },
});
