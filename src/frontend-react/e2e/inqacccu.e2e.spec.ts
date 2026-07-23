import { test, expect } from '@playwright/test'

test.describe('INQACCCU integrated inquiry flow', () => {
  test('renders successful account inquiry through frontend and backend', async ({ page }) => {
    await page.goto('http://localhost:5173/customer-accounts')

    await page.getByLabel('Customer Number').fill('0000000001')
    await page.getByRole('button', { name: 'Inquire' }).click()

    await expect(page.getByText('Inquiry successful')).toBeVisible()
    await expect(page.getByRole('heading', { name: 'Customer Summary' })).toBeVisible()
    await expect(page.getByText('John Smith')).toBeVisible()
    await expect(page.getByRole('heading', { name: 'Accounts (2)' })).toBeVisible()
    await expect(page.getByText('1000000001')).toBeVisible()
  })

  test('supports subsequent inquiry and updates to latest completed result', async ({ page }) => {
    await page.goto('http://localhost:5173/customer-accounts')

    await page.getByLabel('Customer Number').fill('0000000001')
    await page.getByRole('button', { name: 'Inquire' }).click()
    await expect(page.getByText('John Smith')).toBeVisible()

    await page.getByLabel('Customer Number').fill('0000000002')
    await page.getByRole('button', { name: 'Inquire' }).click()

    await expect(page.getByText('Priya Patel')).toBeVisible()
    await expect(page.getByText('2000000001')).toBeVisible()
  })

  test('renders customer-not-found business outcome from backend response', async ({ page }) => {
    await page.goto('http://localhost:5173/customer-accounts')

    await page.getByLabel('Customer Number').fill('0000000999')
    await page.getByRole('button', { name: 'Inquire' }).click()

    await expect(page.getByText('Customer not found')).toBeVisible()
    await expect(page.getByText('1001')).toBeVisible()
  })
})
