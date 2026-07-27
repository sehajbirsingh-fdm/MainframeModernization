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
          legacyStatus: { success: 'Y', failCode: '0', customerFound: 'Y' },
          customerNumber: '0000000001',
          numberOfAccounts: 1,
          accounts: [
            {
              eyecatcher: 'ACCT',
              customerNumber: '0000000001',
              accountNumber: '1000000001',
              sortCode: '987654',
              accountType: 'CHK',
              interestRate: 0.5,
              openedDate: '2020-01-15',
              overdraftLimit: 500,
              lastStatementDate: '2025-12-31',
              nextStatementDate: '2026-01-31',
              availableBalance: 1520.45,
              actualBalance: 1498.12,
            },
          ],
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
          legacyStatus: { success: 'N', failCode: '1', customerFound: 'N' },
          customerNumber: '0000000099',
          numberOfAccounts: 0,
          accounts: [],
        }),
        { status: 200, headers: { 'content-type': 'application/json' } },
      ),
    )

    renderPage()
    const user = userEvent.setup()

    await user.type(screen.getByLabelText('Customer Number'), '0000000099')
    await user.click(screen.getByRole('button', { name: 'Inquire' }))

    expect(await screen.findByText('Customer not found')).toBeInTheDocument()
    expect(screen.getByText('Legacy Status')).toBeInTheDocument()
    expect(screen.queryByText('Account retrieval failed. Please retry later.')).not.toBeInTheDocument()
    expect(screen.queryByText('Backend Error')).not.toBeInTheDocument()
  })

  it('renders retrieval open-stage business failure for failCode 2', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(
        JSON.stringify({
          legacyStatus: { success: 'N', failCode: '2', customerFound: 'Y' },
          customerNumber: '0000000200',
          numberOfAccounts: 0,
          accounts: [],
        }),
        { status: 200, headers: { 'content-type': 'application/json' } },
      ),
    )

    renderPage()
    const user = userEvent.setup()

    await user.type(screen.getByLabelText('Customer Number'), '0000000200')
    await user.click(screen.getByRole('button', { name: 'Inquire' }))

    expect(await screen.findByText('Account retrieval failed. Please retry later.')).toBeInTheDocument()
    expect(screen.queryByText('Customer not found')).not.toBeInTheDocument()
    expect(screen.queryByText('Backend Error')).not.toBeInTheDocument()
  })

  it('renders retrieval fetch-stage business failure for failCode 3', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(
        JSON.stringify({
          legacyStatus: { success: 'N', failCode: '3', customerFound: 'Y' },
          customerNumber: '0000000300',
          numberOfAccounts: 0,
          accounts: [],
        }),
        { status: 200, headers: { 'content-type': 'application/json' } },
      ),
    )

    renderPage()
    const user = userEvent.setup()

    await user.type(screen.getByLabelText('Customer Number'), '0000000300')
    await user.click(screen.getByRole('button', { name: 'Inquire' }))

    expect(await screen.findByText('Account retrieval failed. Please retry later.')).toBeInTheDocument()
    expect(screen.queryByText('Customer not found')).not.toBeInTheDocument()
    expect(screen.queryByText('Backend Error')).not.toBeInTheDocument()
  })

  it('renders retrieval close-stage business failure for failCode 4', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(
        JSON.stringify({
          legacyStatus: { success: 'N', failCode: '4', customerFound: 'Y' },
          customerNumber: '0000000400',
          numberOfAccounts: 0,
          accounts: [],
        }),
        { status: 200, headers: { 'content-type': 'application/json' } },
      ),
    )

    renderPage()
    const user = userEvent.setup()

    await user.type(screen.getByLabelText('Customer Number'), '0000000400')
    await user.click(screen.getByRole('button', { name: 'Inquire' }))

    expect(await screen.findByText('Account retrieval failed. Please retry later.')).toBeInTheDocument()
    expect(screen.queryByText('Customer not found')).not.toBeInTheDocument()
    expect(screen.queryByText('Backend Error')).not.toBeInTheDocument()
  })

  it('renders backend 500 error response', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(JSON.stringify({ error: { type: 'INFRASTRUCTURE_ERROR', message: 'Service unavailable due to infrastructure failure' } }), {
        status: 500,
        headers: { 'content-type': 'application/json' },
      }),
    )

    renderPage()
    const user = userEvent.setup()

    await user.type(screen.getByLabelText('Customer Number'), '0000000001')
    await user.click(screen.getByRole('button', { name: 'Inquire' }))

    expect((await screen.findAllByText('Service unavailable due to infrastructure failure')).length).toBeGreaterThan(0)
    expect(screen.getByText('INFRASTRUCTURE_ERROR')).toBeInTheDocument()
    expect(screen.queryByText('Account retrieval failed. Please retry later.')).not.toBeInTheDocument()
  })

  it('supports subsequent inquiry update and shows latest result', async () => {
    const fetchSpy = vi.spyOn(globalThis, 'fetch')
    fetchSpy
      .mockResolvedValueOnce(
        new Response(
          JSON.stringify({
            legacyStatus: { success: 'Y', failCode: '0', customerFound: 'Y' },
            customerNumber: '0000000001',
            numberOfAccounts: 1,
            accounts: [
              {
                eyecatcher: 'ACCT',
                customerNumber: '0000000001',
                accountNumber: '1000000001',
                sortCode: '987654',
                accountType: 'CHK',
                interestRate: 0.5,
                openedDate: '2020-01-15',
                overdraftLimit: 500,
                lastStatementDate: '2025-12-31',
                nextStatementDate: '2026-01-31',
                availableBalance: 1520.45,
                actualBalance: 1498.12,
              },
            ],
          }),
          { status: 200, headers: { 'content-type': 'application/json' } },
        ),
      )
      .mockResolvedValueOnce(
        new Response(
          JSON.stringify({
            legacyStatus: { success: 'Y', failCode: '0', customerFound: 'Y' },
            customerNumber: '0000000002',
            numberOfAccounts: 0,
            accounts: [],
          }),
          { status: 200, headers: { 'content-type': 'application/json' } },
        ),
      )

    renderPage()
    const user = userEvent.setup()

    await user.type(screen.getByLabelText('Customer Number'), '0000000001')
    await user.click(screen.getByRole('button', { name: 'Inquire' }))
    await screen.findByText('Accounts (1)')

    await user.clear(screen.getByLabelText('Customer Number'))
    await user.type(screen.getByLabelText('Customer Number'), '0000000002')
    await user.click(screen.getByRole('button', { name: 'Inquire' }))

    expect(await screen.findByText('0000000002')).toBeInTheDocument()
    expect(screen.getByText('No accounts found for this customer.')).toBeInTheDocument()
    expect(fetchSpy).toHaveBeenCalledTimes(2)
  })
})
