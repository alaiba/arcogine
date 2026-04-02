import { test, expect } from '@playwright/test'

async function closeWelcomeIfPresent(page) {
  const closeButton = page.getByRole('button', { name: 'Close' })
  if (await closeButton.isVisible().catch(() => false)) {
    await closeButton.click()
  }
}

async function loadScenario(page, label = 'Basic') {
  const welcomeLoad = page.getByRole('button', { name: `Load ${label}` })
  if (await welcomeLoad.isVisible().catch(() => false)) {
    await welcomeLoad.click()
    return
  }

  const scenarioSelect = page.getByRole('combobox', { name: 'Scenario' })
  await scenarioSelect.selectOption({ label })
  await page.getByRole('button', { name: /^Load$/ }).click()
  await expect(page.getByRole('button', { name: /^Run$/ })).toBeEnabled({ timeout: 10_000 })
}

test.describe('Arcogine UI Smoke Tests', () => {
  test('welcome overlay appears on first load', async ({ page }) => {
    await page.goto('/')
    await expect(page.getByText('Welcome to Arcogine')).toBeVisible({
      timeout: 10_000,
    })
  })

  test('can load a scenario and see KPI cards', async ({ page }) => {
    await page.goto('/')
    await loadScenario(page, 'Basic')

    await expect(page.getByText('Revenue', { exact: true })).toBeVisible({ timeout: 10_000 })
    await expect(page.getByText('Backlog')).toBeVisible({ timeout: 10_000 })
  })

  test('scenario selector loads and run updates controls', async ({ page }) => {
    await page.goto('/')
    await closeWelcomeIfPresent(page)

    const select = page.getByRole('combobox', { name: 'Scenario' })
    await select.selectOption({ label: 'Overload' })
    await page.getByRole('button', { name: /^Load$/ }).click()
    await expect(page.getByRole('button', { name: /^Run$/ })).toBeEnabled({ timeout: 10_000 })

    await page.getByRole('button', { name: /^Run$/ }).click()
    await expect(page.getByRole('button', { name: /^Pause$/ })).toBeVisible({ timeout: 10_000 })
  })

  test('event log drawer can be expanded', async ({ page }) => {
    await page.goto('/')
    await loadScenario(page, 'Basic')
    await expect(page.getByText('Revenue')).toBeVisible({ timeout: 10_000 })

    const toggle = page.getByRole('button', { name: /event log/i })
    await expect(toggle).toBeVisible({ timeout: 5_000 })
    await toggle.click()
    await expect(page.getByText(/No events match/i)).toBeVisible({ timeout: 10_000 })
  })

  test('factory flow tab shows machine topology graph', async ({ page }) => {
    await page.goto('/')
    await expect(page.getByRole('button', { name: 'Load Basic' })).toBeVisible({
      timeout: 10_000,
    })
    await page.getByRole('button', { name: 'Load Basic' }).click()

    const flowTab = page.getByRole('button', { name: /Factory Flow/ })
    await flowTab.click()
    await expect(page.getByRole('img', { name: 'Factory routing graph' })).toBeVisible({ timeout: 10_000 })
  })
})
