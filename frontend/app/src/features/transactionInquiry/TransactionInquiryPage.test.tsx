import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { BrowserRouter } from 'react-router-dom'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { TransactionInquiryPage } from './TransactionInquiryPage'

function renderPage() {
  const queryClient = new QueryClient()
  return render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <TransactionInquiryPage />
      </BrowserRouter>
    </QueryClientProvider>,
  )
}

function successPayload(overrides: Record<string, unknown> = {}) {
  return {
    sortCode: '123456',
    accountNumber: '00000001',
    fromDate: null,
    toDate: null,
    limit: 50,
    offset: 0,
    totalCount: 1,
    returnedCount: 1,
    transactions: [
      {
        transactionId: '123456-00000001-20260728-143015-000000000123',
        sortCode: '123456',
        accountNumber: '00000001',
        date: '20260728',
        time: '143015',
        reference: '000000000123',
        type: 'CRD',
        description: 'Payroll deposit',
        amount: 125.5,
      },
    ],
    ...overrides,
  }
}

describe('TransactionInquiryPage', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('shows validation errors for malformed values', async () => {
    renderPage()
    const user = userEvent.setup()

    await user.type(screen.getByLabelText('Sort Code'), '12')
    await user.type(screen.getByLabelText('Account Number'), '1')
    await user.click(screen.getByRole('button', { name: 'Inquire Transactions' }))

    expect(screen.getByText('Sort code must be exactly 6 digits.')).toBeInTheDocument()
    expect(screen.getByText('Account number must be exactly 8 digits.')).toBeInTheDocument()
  })

  it('renders populated results and metadata', async () => {
    const fetchSpy = vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(JSON.stringify(successPayload()), {
        status: 200,
        headers: {
          'content-type': 'application/json',
          'X-Correlation-ID': 'corr-success',
        },
      }),
    )

    renderPage()
    const user = userEvent.setup()

    await user.type(screen.getByLabelText('Sort Code'), '123456')
    await user.type(screen.getByLabelText('Account Number'), '00000001')
    await user.click(screen.getByRole('button', { name: 'Inquire Transactions' }))

    await screen.findByText('Transaction inquiry successful.')

    expect(fetchSpy).toHaveBeenCalledWith(
      '/api/v1/accounts/123456/00000001/transactions?limit=50&offset=0',
      expect.any(Object),
    )
    expect(screen.getByText('Result Metadata')).toBeInTheDocument()
    expect(screen.getByText('Transactions')).toBeInTheDocument()
    expect(screen.getByText('123456-00000001-20260728-143015-000000000123')).toBeInTheDocument()
    const detailLink = screen.getByRole('link', { name: 'View Detail' })
    expect(detailLink).toHaveAttribute('href', '/transactions/123456/00000001/20260728/143015/000000000123')
  })

  it('renders empty-success state', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(
        JSON.stringify(
          successPayload({
            totalCount: 0,
            returnedCount: 0,
            transactions: [],
          }),
        ),
        {
          status: 200,
          headers: {
            'content-type': 'application/json',
            'X-Correlation-ID': 'corr-empty',
          },
        },
      ),
    )

    renderPage()
    const user = userEvent.setup()

    await user.type(screen.getByLabelText('Sort Code'), '123456')
    await user.type(screen.getByLabelText('Account Number'), '00000001')
    await user.click(screen.getByRole('button', { name: 'Inquire Transactions' }))

    expect(await screen.findByText('No transactions matched the supplied criteria.')).toBeInTheDocument()
  })

  it('renders technical-error state safely', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(
        JSON.stringify({
          code: 'ERR-500',
          message: 'Service unavailable due to infrastructure failure',
          correlationId: 'corr-500',
        }),
        {
          status: 500,
          headers: {
            'content-type': 'application/json',
            'X-Correlation-ID': 'corr-500',
          },
        },
      ),
    )

    renderPage()
    const user = userEvent.setup()

    await user.type(screen.getByLabelText('Sort Code'), '123456')
    await user.type(screen.getByLabelText('Account Number'), '00000001')
    await user.click(screen.getByRole('button', { name: 'Inquire Transactions' }))

    expect(await screen.findByText('Service unavailable due to infrastructure failure')).toBeInTheDocument()
    expect(screen.getByText('ERR-500')).toBeInTheDocument()
  })

  it('replaces previous completed result on subsequent inquiry', async () => {
    vi.spyOn(globalThis, 'fetch')
      .mockResolvedValueOnce(
        new Response(JSON.stringify(successPayload()), {
          status: 200,
          headers: {
            'content-type': 'application/json',
            'X-Correlation-ID': 'corr-first',
          },
        }),
      )
      .mockResolvedValueOnce(
        new Response(
          JSON.stringify(
            successPayload({
              transactions: [
                {
                  transactionId: '123456-00000001-20260720-121500-000000000999',
                  sortCode: '123456',
                  accountNumber: '00000001',
                  date: '20260720',
                  time: '121500',
                  reference: '000000000999',
                  type: 'DBT',
                  description: 'Updated record',
                  amount: -5.25,
                },
              ],
            }),
          ),
          {
            status: 200,
            headers: {
              'content-type': 'application/json',
              'X-Correlation-ID': 'corr-second',
            },
          },
        ),
      )

    renderPage()
    const user = userEvent.setup()

    await user.type(screen.getByLabelText('Sort Code'), '123456')
    await user.type(screen.getByLabelText('Account Number'), '00000001')
    await user.click(screen.getByRole('button', { name: 'Inquire Transactions' }))
    await screen.findByText('123456-00000001-20260728-143015-000000000123')

    await user.clear(screen.getByLabelText('Offset'))
    await user.type(screen.getByLabelText('Offset'), '2')
    await user.click(screen.getByRole('button', { name: 'Inquire Transactions' }))

    await screen.findByText('123456-00000001-20260720-121500-000000000999')
    await waitFor(() => {
      expect(screen.queryByText('123456-00000001-20260728-143015-000000000123')).not.toBeInTheDocument()
    })
  })
})
