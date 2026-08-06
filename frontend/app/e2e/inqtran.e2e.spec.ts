import { test, expect } from '@playwright/test'

test.describe('INQTRAN integrated inquiry flow', () => {
  test('renders populated transaction inquiry result', async ({ page }) => {
    await page.goto('http://localhost:5173/transactions')

    await page.getByLabel('Sort Code').fill('123456')
    await page.getByLabel('Account Number').fill('00000001')
    await page.getByRole('button', { name: 'Inquire Transactions' }).click()

    await expect(page.getByText('Transaction inquiry successful.')).toBeVisible()
    await expect(page.getByRole('heading', { name: 'Result Metadata' })).toBeVisible()
    await expect(page.getByRole('heading', { name: 'Transactions' })).toBeVisible()
    await expect(page.getByText('123456-00000001-20260728-143015-000000000123')).toBeVisible()
  })

  test('renders empty-success outcome with no transaction-detail navigation', async ({ page }) => {
    await page.goto('http://localhost:5173/transactions')

    await page.getByLabel('Sort Code').fill('123456')
    await page.getByLabel('Account Number').fill('00000001')
    await page.getByLabel('From Date (optional)').fill('20990101')
    await page.getByLabel('To Date (optional)').fill('20990131')
    await page.getByRole('button', { name: 'Inquire Transactions' }).click()

    await expect(page.getByText('No transactions matched the supplied criteria.')).toBeVisible()
    await expect(page.getByText('No transactions returned for this page.')).toBeVisible()
    await expect(page.getByRole('link', { name: /transaction detail/i })).toHaveCount(0)
  })
})
