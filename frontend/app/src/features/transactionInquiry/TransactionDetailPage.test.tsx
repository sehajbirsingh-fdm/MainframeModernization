import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { TransactionDetailPage } from './TransactionDetailPage'

function renderDetailPage(path: string) {
  const queryClient = new QueryClient()
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter initialEntries={[path]}>
        <Routes>
          <Route path="/transactions/:sortCode/:accountNumber/:date/:time/:reference" element={<TransactionDetailPage />} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

describe('TransactionDetailPage', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('renders found detail response from approved endpoint', async () => {
    const fetchSpy = vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(
        JSON.stringify({
          found: true,
          transaction: {
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
        }),
        {
          status: 200,
          headers: {
            'content-type': 'application/json',
            'X-Correlation-ID': 'corr-found',
          },
        },
      ),
    )

    renderDetailPage('/transactions/123456/00000001/20260728/143015/000000000123')

    expect(await screen.findByText('Transaction detail retrieved successfully.')).toBeInTheDocument()
    expect(await screen.findByText('123456-00000001-20260728-143015-000000000123')).toBeInTheDocument()
    expect(fetchSpy).toHaveBeenCalledWith(
      '/api/v1/accounts/123456/00000001/transactions/20260728/143015/000000000123',
      expect.any(Object),
    )
  })

  it('renders successful absence as found=false with null transaction', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(
        JSON.stringify({
          found: false,
          transaction: null,
        }),
        {
          status: 200,
          headers: {
            'content-type': 'application/json',
            'X-Correlation-ID': 'corr-missing',
          },
        },
      ),
    )

    renderDetailPage('/transactions/123456/00000001/20990101/000000/999999999999')

    expect(await screen.findByText('No transaction detail was found for the supplied identity.')).toBeInTheDocument()
  })

  it('renders technical failure details', async () => {
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

    renderDetailPage('/transactions/123456/00000001/20260728/143015/000000000123')

    expect(await screen.findByText('Service unavailable due to infrastructure failure')).toBeInTheDocument()
    expect(screen.getByText('ERR-500')).toBeInTheDocument()
    expect(screen.getByText('corr-500')).toBeInTheDocument()
  })
})
