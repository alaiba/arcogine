// Real-browser end-to-end tests: the browser drives the real Java server and the real engine.
// Nothing here mocks a simulation result; the only interception is aborting a request to prove the
// client reports a disconnected server honestly.
import { expect, test } from '@playwright/test';

const cell = (page, x, y) => page.getByTestId(`cell-${x}-${y}`);
const text = (page, id) => page.getByTestId(id);

async function place(page, item, x, y) {
  const button = page.getByTestId(`buy-${item}`);
  if ((await button.getAttribute('aria-pressed')) !== 'true') await button.click();
  await cell(page, x, y).click();
}

// Presses Step until the run ends. Every step adds at least one supported event to the timeline, so a
// changed event count proves the server answered before the next click.
async function stepToEnd(page, max) {
  const step = page.getByTestId('step');
  const verdict = text(page, 'verdict');
  const events = page.locator('#events li');
  for (let i = 0; i < max; i += 1) {
    if (await verdict.isVisible()) return;
    const before = await events.count();
    await step.click();
    await expect.poll(async () => (await events.count()) !== before).toBe(true);
  }
}

async function playToEnd(page, speed = '4') {
  await page.getByTestId('speed').selectOption(speed);
  await page.getByTestId('play').click();
  await expect(text(page, 'verdict')).toBeVisible({ timeout: 30_000 });
}

const pageErrors = [];

test.beforeEach(async ({ page }) => {
  pageErrors.length = 0;
  page.on('pageerror', (error) => pageErrors.push(error.message));
  await page.goto('/');
  await expect(page.getByTestId('tab-the-relay')).toBeVisible();
});

test.afterEach(() => {
  expect(pageErrors, 'the client must not throw').toEqual([]);
});

test('Wake the Foundry: the starter draft is admissible but unpublishable until the player places an inspector', async ({ page }) => {
  await page.getByTestId('check').click();
  await expect(text(page, 'report-contract')).toContainText('admissible');
  await expect(text(page, 'report-factory')).toContainText('cannot be published');
  await page.getByTestId('publish').click();
  await expect(page.locator('#banner')).toContainText('cannot be run');
  await expect(page.getByTestId('step')).toBeDisabled();
});

test('Wake the Foundry: a human places an inspector, publishes, steps and ships the first order', async ({ page }) => {
  await place(page, 'inspector', 7, 2);
  await page.getByTestId('check').click();
  await expect(text(page, 'report-factory')).toContainText('can be published');
  await page.getByTestId('publish').click();
  await expect(text(page, 'order-progress')).toHaveText('0 / 3');
  await expect(text(page, 'events')).toContainText('Order 1 accepted');

  // Single-step: the clock only moves when the engine processes an event.
  await page.getByTestId('step').click();
  await expect(text(page, 'time')).toHaveText('t = 2');
  await stepToEnd(page, 20);
  await expect(text(page, 'verdict')).toHaveText('Contract fulfilled');
  await expect(text(page, 'order-progress')).toHaveText('3 / 3');
  await expect(text(page, 'time')).toHaveText('t = 13');
  await expect(text(page, 'result-reference')).toHaveText('98');
  await expect(text(page, 'events')).toContainText('shipped');
});

test('The Relay: a cheap line fails the deadline, an improved design wins, and the attempts are compared', async ({ page }) => {
  await page.getByTestId('tab-the-relay').click();
  await place(page, 'cutter', 1, 3);
  await place(page, 'assembler', 4, 3);
  await place(page, 'inspector', 7, 2);
  await page.getByTestId('publish').click();
  await playToEnd(page);
  await expect(text(page, 'verdict')).toHaveText('Contract failed');
  await expect(text(page, 'issue')).toContainText('challenge.deadline.missed');
  await expect(text(page, 'time')).toHaveText('t = 67');
  await expect(text(page, 'result-reference')).toHaveText('0');

  await page.getByTestId('edit').click();
  await place(page, 'inspector', 7, 4);
  await page.getByTestId('check').click();
  await expect(text(page, 'budget')).toContainText('212 / 1062');
  await page.getByTestId('publish').click();
  await playToEnd(page);
  await expect(text(page, 'verdict')).toHaveText('Contract fulfilled');
  await expect(text(page, 'time')).toHaveText('t = 56');
  await expect(text(page, 'result-reference')).toHaveText('214');
  await expect(text(page, 'comparison')).toContainText('this attempt scored higher');
});

test('The Relay: a structurally different twin-assembler design also wins, with a different score', async ({ page }) => {
  await page.getByTestId('tab-the-relay').click();
  await place(page, 'cutter', 1, 3);
  await place(page, 'twin-assembler', 4, 3);
  await place(page, 'inspector', 7, 2);
  await place(page, 'inspector', 7, 4);
  await page.getByTestId('publish').click();
  await playToEnd(page);
  await expect(text(page, 'verdict')).toHaveText('Contract fulfilled');
  await expect(text(page, 'time')).toHaveText('t = 45');
  await expect(text(page, 'result-reference')).toHaveText('65');
});

test('presentation speed and stepping never change the result; retry creates a fresh run', async ({ page }) => {
  await page.getByTestId('tab-the-relay').click();
  await place(page, 'cutter', 1, 3);
  await place(page, 'assembler', 4, 3);
  await place(page, 'inspector', 7, 2);
  await place(page, 'inspector', 7, 4);
  await page.getByTestId('publish').click();
  await playToEnd(page, '4');
  const fast = { time: await text(page, 'time').innerText(), score: await text(page, 'result-reference').innerText() };

  await page.getByTestId('reset').click();
  await expect(text(page, 'time')).toHaveText('t = 0');
  await expect(text(page, 'verdict')).toBeHidden();
  await stepToEnd(page, 40);
  await expect(text(page, 'verdict')).toBeVisible();
  expect({ time: await text(page, 'time').innerText(), score: await text(page, 'result-reference').innerText() }).toEqual(fast);
});

test('the player can move, undo and sell equipment with keyboard alternatives', async ({ page }) => {
  await page.getByTestId('tab-the-relay').click();
  await page.getByTestId('buy-cutter').click();
  await page.getByTestId('floor').focus();
  await page.keyboard.press('ArrowRight');
  await page.keyboard.press('Enter');
  await expect(page.getByTestId('machine-1')).toBeVisible();
  await page.keyboard.press('Shift+ArrowDown');
  await expect(text(page, 'inspector')).toContainText('Cell (1, 1)');
  await page.keyboard.press('Control+z');
  await expect(text(page, 'inspector')).toContainText('Cell (1, 0)');
  await page.keyboard.press('Delete');
  await expect(page.getByTestId('machine-1')).toBeHidden();
});

test('an unreachable server is reported as a disconnect without changing the displayed run', async ({ page }) => {
  await place(page, 'inspector', 7, 2);
  await page.getByTestId('publish').click();
  await expect(text(page, 'order-progress')).toHaveText('0 / 3');
  await page.route('**/api/runs/*/step', (route) => route.abort());
  await page.getByTestId('step').click();
  await expect(page.locator('#banner')).toContainText('Disconnected');
  await expect(text(page, 'time')).toHaveText('t = 0');
  await page.unroute('**/api/runs/*/step');
  await page.getByTestId('step').click();
  await expect(text(page, 'time')).toHaveText('t = 2');
});

test('retrying while playback is running is never overwritten by a late response from the old run', async ({ page }) => {
  await page.getByTestId('tab-the-relay').click();
  await place(page, 'cutter', 1, 3);
  await place(page, 'assembler', 4, 3);
  await place(page, 'inspector', 7, 2);
  await place(page, 'inspector', 7, 4);
  await page.getByTestId('publish').click();
  await page.getByTestId('speed').selectOption('4');
  await page.getByTestId('play').click();
  await page.getByTestId('reset').click();
  await expect(text(page, 'time')).toHaveText('t = 0');
  // Give any in-flight playback response time to arrive; the fresh run must stay untouched.
  await page.waitForTimeout(1500);
  await expect(text(page, 'time')).toHaveText('t = 0');
  await expect(text(page, 'verdict')).toBeHidden();
  await expect(page.getByTestId('play')).toHaveText('Play');
  await page.getByTestId('step').click();
  await expect(text(page, 'time')).toHaveText('t = 3');
});

// Holds the first /advance request across Pause then Play and records how many advance requests are
// ever in flight at once. `settle` decides how the held request ends once the player has resumed.
async function holdAdvanceAcrossPauseAndPlay(page, settle) {
  await page.getByTestId('tab-the-relay').click();
  await place(page, 'cutter', 1, 3);
  await place(page, 'assembler', 4, 3);
  await place(page, 'inspector', 7, 2);
  await place(page, 'inspector', 7, 4);
  await page.getByTestId('publish').click();
  await page.getByTestId('speed').selectOption('3');

  let release;
  const held = new Promise((resolve) => { release = resolve; });
  let requests = 0;
  let inFlight = 0;
  let maxInFlight = 0;
  const isAdvance = (request) => request.url().includes('/advance');
  page.on('request', (request) => {
    if (isAdvance(request)) { inFlight += 1; maxInFlight = Math.max(maxInFlight, inFlight); }
  });
  const done = (request) => { if (isAdvance(request)) inFlight -= 1; };
  page.on('requestfinished', done);
  page.on('requestfailed', done);
  await page.route('**/api/runs/*/advance', async (route) => {
    requests += 1;
    if (requests === 1) {
      await held;
      await settle(route);
    } else {
      await route.continue();
    }
  });

  const play = page.getByTestId('play');
  await play.click();
  await expect.poll(() => requests).toBeGreaterThanOrEqual(1);
  await play.click();
  await expect(play).toHaveText('Play');
  await play.click();
  await expect(play).toHaveText('Pause');
  await page.waitForTimeout(500);
  expect(requests, 'the resumed loop waits for the held request instead of overlapping it').toBe(1);
  release();
  return { play, stats: () => ({ requests, maxInFlight }) };
}

test('pausing and resuming while a request is held keeps a single playback loop and applies the response', async ({ page }) => {
  const { play, stats } = await holdAdvanceAcrossPauseAndPlay(page, (route) => route.continue());
  await expect(text(page, 'verdict')).toBeVisible({ timeout: 30_000 });
  await expect(text(page, 'time')).toHaveText('t = 56');
  expect(stats().maxInFlight).toBe(1);
  await expect(play).toHaveText('Play');
});

test('a superseded loop whose held request fails neither stops the new playback nor shows an error', async ({ page }) => {
  const { stats } = await holdAdvanceAcrossPauseAndPlay(page, (route) => route.abort());
  await expect(text(page, 'verdict')).toBeVisible({ timeout: 30_000 });
  await expect(text(page, 'time')).toHaveText('t = 56');
  await expect(page.locator('#banner')).not.toContainText('Disconnected');
  expect(stats().maxInFlight).toBe(1);
});
