import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { BrowserRouter } from 'react-router-dom'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { StatementInquiryPage } from './StatementInquiryPage'

function renderPage() {
  const queryClient = new QueryClient()
  return render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <StatementInquiryPage />
      </BrowserRouter>
    </QueryClientProvider>,
  )
}

function statementSuccessBody(overrides: Record<string, unknown> = {}) {
  return {
    sortCode: '123456',
    accountNumber: '00000077',
    period: '202607',
    summary: {
      periodFrom: '20260701',
      periodTo: '20260731',
      openingBalance: 0,
      totalCredits: 100,
      totalDebits: 0,
      closingBalance: 100,
      transactionCount: 1,
    },
    entries: [
      {
        date: '20260708',
        time: '091500',
        reference: '000000000132',
        type: 'CRD',
        description: 'Standalone account credit',
        amount: 100,
      },
    ],
    ...overrides,
  }
}

describe('StatementInquiryPage', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('shows validation errors for malformed values', async () => {
    renderPage()
    const user = userEvent.setup()

    await user.type(screen.getByLabelText('Sort Code'), '1')
    await user.type(screen.getByLabelText('Account Number'), '2')
    await user.type(screen.getByLabelText('Statement Period (YYYYMM)'), '202613')
    await user.click(screen.getByRole('button', { name: 'Retrieve Statement' }))

    expect(screen.getByText('Sort code must be exactly 6 digits.')).toBeInTheDocument()
    expect(screen.getByText('Account number must be exactly 8 digits.')).toBeInTheDocument()
    expect(screen.getByText('Period must be YYYYMM with month between 01 and 12.')).toBeInTheDocument()
  })

  it('renders statement summary and entries on success', async () => {
    const fetchSpy = vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(JSON.stringify(statementSuccessBody()), {
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
    await user.type(screen.getByLabelText('Account Number'), '00000077')
    await user.type(screen.getByLabelText('Statement Period (YYYYMM)'), '202607')
    await user.click(screen.getByRole('button', { name: 'Retrieve Statement' }))

    expect(fetchSpy).toHaveBeenCalledWith(
      '/api/v1/accounts/123456/00000077/statements/202607',
      expect.any(Object),
    )

    expect(await screen.findByText('Statement retrieval successful.')).toBeInTheDocument()
    expect(screen.getByText('Statement Summary')).toBeInTheDocument()
    expect(screen.getByText('000000000132')).toBeInTheDocument()
  })

  it('renders no-entry success message when statement has zero entries', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(
        JSON.stringify(
          statementSuccessBody({
            summary: {
              periodFrom: '20260701',
              periodTo: '20260731',
              openingBalance: 0,
              totalCredits: 0,
              totalDebits: 0,
              closingBalance: 0,
              transactionCount: 0,
            },
            entries: [],
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
    await user.type(screen.getByLabelText('Account Number'), '00000077')
    await user.type(screen.getByLabelText('Statement Period (YYYYMM)'), '202607')
    await user.click(screen.getByRole('button', { name: 'Retrieve Statement' }))

    expect(await screen.findByText('No statement entries found for the selected period.')).toBeInTheDocument()
  })

  it('renders backend errors and error details', async () => {
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
    await user.type(screen.getByLabelText('Account Number'), '00000077')
    await user.type(screen.getByLabelText('Statement Period (YYYYMM)'), '202607')
    await user.click(screen.getByRole('button', { name: 'Retrieve Statement' }))

    expect(await screen.findByText('Service unavailable due to infrastructure failure')).toBeInTheDocument()
    expect(screen.getByText('ERR-500')).toBeInTheDocument()
  })
})
