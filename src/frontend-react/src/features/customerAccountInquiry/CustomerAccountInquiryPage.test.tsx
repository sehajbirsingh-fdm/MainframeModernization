import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { BrowserRouter } from 'react-router-dom'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { CustomerAccountInquiryPage } from './CustomerAccountInquiryPage'

function renderPage() {
  const queryClient = new QueryClient()
  return render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <CustomerAccountInquiryPage />
      </BrowserRouter>
    </QueryClientProvider>,
  )
}

describe('CustomerAccountInquiryPage', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('shows validation error for malformed customer number', async () => {
    renderPage()
    const user = userEvent.setup()

    await user.type(screen.getByLabelText('Customer Number'), '123')
    await user.click(screen.getByRole('button', { name: 'Inquire' }))

    expect(screen.getByText('Customer number must be exactly 10 digits.')).toBeInTheDocument()
  })

  it('renders success response with accounts', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(
        JSON.stringify({
          legacyStatus: { success: 'Y', failCode: '0000', customerFound: 'Y' },
          customer: {
            customerNumber: '0000000001',
            customerName: 'John Smith',
            sortCode: '123456',
            customerType: 'INDIVIDUAL',
          },
          accounts: {
            count: 1,
            accounts: [
              {
                accountNumber: '1000000001',
                sortCode: '123456',
                accountType: 'CHK',
                accountTypeDescription: 'Checking Account',
                availableBalance: 1520.45,
                availableBalanceCurrency: 'GBP',
                actualBalance: 1498.12,
                actualBalanceCurrency: 'GBP',
                interestRate: 0.5,
                overdraftLimit: 500,
                lastStatementDate: '2025-12-31',
                nextStatementDate: '2026-01-31',
              },
            ],
          },
        }),
        { status: 200, headers: { 'content-type': 'application/json' } },
      ),
    )

    renderPage()
    const user = userEvent.setup()

    await user.type(screen.getByLabelText('Customer Number'), '0000000001')
    await user.click(screen.getByRole('button', { name: 'Inquire' }))

    expect(await screen.findByText('Inquiry successful')).toBeInTheDocument()
    expect(screen.getByText('Customer Summary')).toBeInTheDocument()
    expect(screen.getByText('Accounts (1)')).toBeInTheDocument()
  })

  it('renders not-found business outcome inside 200 payload', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(
        JSON.stringify({
          legacyStatus: { success: 'N', failCode: '1001', customerFound: 'N' },
          customer: null,
          accounts: null,
        }),
        { status: 200, headers: { 'content-type': 'application/json' } },
      ),
    )

    renderPage()
    const user = userEvent.setup()

    await user.type(screen.getByLabelText('Customer Number'), '0000000099')
    await user.click(screen.getByRole('button', { name: 'Inquire' }))

    expect(await screen.findByText('Customer not found')).toBeInTheDocument()
    expect(screen.getByText('1001')).toBeInTheDocument()
  })

  it('renders backend 500 error response', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(JSON.stringify({ code: 'ERR-005', message: 'Internal processing error', details: [] }), {
        status: 500,
        headers: { 'content-type': 'application/json' },
      }),
    )

    renderPage()
    const user = userEvent.setup()

    await user.type(screen.getByLabelText('Customer Number'), '0000000001')
    await user.click(screen.getByRole('button', { name: 'Inquire' }))

    expect((await screen.findAllByText('Internal processing error')).length).toBeGreaterThan(0)
    expect(screen.getByText('ERR-005')).toBeInTheDocument()
  })

  it('supports subsequent inquiry update and shows latest result', async () => {
    const fetchSpy = vi.spyOn(globalThis, 'fetch')
    fetchSpy
      .mockResolvedValueOnce(
        new Response(
          JSON.stringify({
            legacyStatus: { success: 'Y', failCode: '0000', customerFound: 'Y' },
            customer: {
              customerNumber: '0000000001',
              customerName: 'John Smith',
              sortCode: '123456',
              customerType: 'INDIVIDUAL',
            },
            accounts: { count: 1, accounts: [] },
          }),
          { status: 200, headers: { 'content-type': 'application/json' } },
        ),
      )
      .mockResolvedValueOnce(
        new Response(
          JSON.stringify({
            legacyStatus: { success: 'Y', failCode: '0000', customerFound: 'Y' },
            customer: {
              customerNumber: '0000000002',
              customerName: 'Priya Patel',
              sortCode: '654321',
              customerType: 'INDIVIDUAL',
            },
            accounts: { count: 0, accounts: [] },
          }),
          { status: 200, headers: { 'content-type': 'application/json' } },
        ),
      )

    renderPage()
    const user = userEvent.setup()

    await user.type(screen.getByLabelText('Customer Number'), '0000000001')
    await user.click(screen.getByRole('button', { name: 'Inquire' }))
    await screen.findByText('John Smith')

    await user.clear(screen.getByLabelText('Customer Number'))
    await user.type(screen.getByLabelText('Customer Number'), '0000000002')
    await user.click(screen.getByRole('button', { name: 'Inquire' }))

    expect(await screen.findByText('Priya Patel')).toBeInTheDocument()
    expect(screen.getByText('No accounts found for this customer.')).toBeInTheDocument()
    expect(fetchSpy).toHaveBeenCalledTimes(2)
  })
})
