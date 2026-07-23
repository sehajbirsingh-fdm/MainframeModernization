import { test, expect } from '@playwright/test'

test.describe('INQACCCU integrated inquiry flow', () => {
  test('renders successful account inquiry through frontend and backend', async ({ page }) => {
    await page.goto('http://localhost:5173/customer-accounts')

    await page.getByLabel('Customer Number').fill('0000000001')
    await page.getByRole('button', { name: 'Inquire' }).click()

    await expect(page.getByText('Inquiry successful')).toBeVisible()
    const customerSummary = page.getByLabel('Customer Summary')
    await expect(page.getByRole('heading', { name: 'Customer Summary' })).toBeVisible()
    await expect(customerSummary.getByText('0000000001')).toBeVisible()
    await expect(page.getByRole('heading', { name: 'Accounts (2)' })).toBeVisible()
    await expect(page.getByRole('cell', { name: 'ACCT' })).toHaveCount(2)
    await expect(page.getByText('1000000001')).toBeVisible()
    await expect(page.getByRole('cell', { name: '987654' })).toHaveCount(2)
    await expect(page.getByText('2020-01-15')).toBeVisible()
    await expect(page.getByText('1000000001')).toBeVisible()
    await expect(page.getByRole('heading', { name: 'Backend Error' })).not.toBeVisible()
    await expect(page.getByText('Service unavailable due to infrastructure failure')).not.toBeVisible()
  })

  test('supports subsequent inquiry and updates to latest completed result', async ({ page }) => {
    await page.goto('http://localhost:5173/customer-accounts')

    await page.getByLabel('Customer Number').fill('0000000001')
    await page.getByRole('button', { name: 'Inquire' }).click()
    await expect(page.getByText('Inquiry successful')).toBeVisible()
    await expect(page.getByRole('heading', { name: 'Accounts (2)' })).toBeVisible()

    await page.getByLabel('Customer Number').fill('0000000002')
    await page.getByRole('button', { name: 'Inquire' }).click()

    await expect(page.getByText('Inquiry successful')).toBeVisible()
    const customerSummary = page.getByLabel('Customer Summary')
    await expect(customerSummary.getByText('0000000002')).toBeVisible()
    await expect(page.getByRole('heading', { name: 'Accounts (1)' })).toBeVisible()
    await expect(page.getByText('2000000001')).toBeVisible()
    await expect(page.getByText('2022-03-01')).toBeVisible()
    await expect(page.getByRole('heading', { name: 'Backend Error' })).not.toBeVisible()
    await expect(page.getByText('Service unavailable due to infrastructure failure')).not.toBeVisible()
  })

  test('renders customer-not-found business outcome from backend response', async ({ page }) => {
    await page.goto('http://localhost:5173/customer-accounts')

    await page.getByLabel('Customer Number').fill('0000000999')
    await page.getByRole('button', { name: 'Inquire' }).click()

    await expect(page.getByText('Customer not found')).toBeVisible()
    const customerSummary = page.getByLabel('Customer Summary')
    await expect(customerSummary.getByText('0000000999')).toBeVisible()
    await expect(page.getByRole('heading', { name: 'Accounts (0)' })).not.toBeVisible()
    await expect(page.getByText(/^1$/)).toBeVisible()
    await expect(page.getByRole('heading', { name: 'Backend Error' })).not.toBeVisible()
    await expect(page.getByText('Service unavailable due to infrastructure failure')).not.toBeVisible()
  })
})
