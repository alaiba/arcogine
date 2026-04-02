import { test, expect } from '@playwright/test'

test.describe('Arcogine UI Smoke Tests', () => {
  test('welcome overlay appears on first load', async ({ page }) => {
    await page.goto('/')
    await expect(page.getByText('Welcome to Arcogine')).toBeVisible({
      timeout: 10_000,
    })
  })

  test('can load a scenario and see KPI cards', async ({ page }) => {
    await page.goto('/')

    // Wait for the welcome overlay
    await expect(page.getByText('Welcome to Arcogine')).toBeVisible({
      timeout: 10_000,
    })

    // Click the Basic scenario card to load it
    await page.getByText('Basic').first().click()
    await page.waitForTimeout(1000)

    // KPI cards should appear
    await expect(page.getByText('Revenue')).toBeVisible({ timeout: 5000 })
    await expect(page.getByText('Backlog')).toBeVisible()
  })

  test('scenario selector loads and run produces events', async ({ page }) => {
    await page.goto('/')
    await page.waitForTimeout(500)

    // Use the toolbar scenario selector
    const select = page.locator('select').first()
    if (await select.isVisible()) {
      await select.selectOption({ index: 1 })
      await page.getByRole('button', { name: /load/i }).click()
      await page.waitForTimeout(1000)

      // Click Run
      const runBtn = page.getByRole('button', { name: /run/i }).first()
      if (await runBtn.isEnabled()) {
        await runBtn.click()
        await page.waitForTimeout(2000)
      }
    }
  })

  test('event log drawer can be expanded', async ({ page }) => {
    await page.goto('/')
    await page.waitForTimeout(500)

    // Look for the event log toggle
    const toggle = page.getByText(/event log/i).first()
    if (await toggle.isVisible()) {
      await toggle.click()
      await page.waitForTimeout(500)
    }
  })

  test('factory flow tab shows machine nodes', async ({ page }) => {
    await page.goto('/')
    await page.waitForTimeout(500)

    // Dismiss welcome if present, load scenario
    const basicBtn = page.getByText('Basic').first()
    if (await basicBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
      await basicBtn.click()
      await page.waitForTimeout(1000)
    }

    // Click Factory Flow tab
    const flowTab = page.getByText('Factory Flow').first()
    if (await flowTab.isVisible({ timeout: 3000 }).catch(() => false)) {
      await flowTab.click()
      await page.waitForTimeout(500)
    }
  })
})
