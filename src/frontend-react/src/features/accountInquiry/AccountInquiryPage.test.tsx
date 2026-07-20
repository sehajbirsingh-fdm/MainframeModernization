import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { BrowserRouter } from 'react-router-dom'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { AccountInquiryPage } from './AccountInquiryPage'

function renderPage() {
  const queryClient = new QueryClient()
  return render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <AccountInquiryPage />
      </BrowserRouter>
    </QueryClientProvider>,
  )
}

function accountSuccessBody(overrides: Partial<Record<string, unknown>> = {}) {
  return {
    eyecatcher: 'ACCT',
    customerNumber: '1234567890',
    sortcode: '123456',
    accountNumber: '00000001',
    accountType: 'SAVINGS',
    interestRate: 2.5,
    accountOpened: '2020-01-15',
    overdraftLimit: 5000,
    lastStatementDate: '2024-01-31',
    nextStatementDate: '2024-02-28',
    availableBalance: 15750.5,
    actualBalance: 15750.5,
    ...overrides,
  }
}

describe('AccountInquiryPage', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('shows validation errors for malformed values', async () => {
    renderPage()
    const user = userEvent.setup()

    await user.type(screen.getByLabelText('Sortcode'), '12')
    await user.type(screen.getByLabelText('Account Number'), '123')
    await user.click(screen.getByRole('button', { name: 'Inquire Account' }))

    expect(screen.getByText('Sortcode must be exactly 6 digits.')).toBeInTheDocument()
    expect(screen.getByText('Account number must be exactly 8 digits.')).toBeInTheDocument()
  })

  it('renders all twelve account fields on success', async () => {
    const fetchSpy = vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(JSON.stringify(accountSuccessBody()), {
        status: 200,
        headers: {
          'content-type': 'application/json',
          'X-Correlation-ID': 'corr-success',
        },
      }),
    )

    renderPage()
    const user = userEvent.setup()

    await user.type(screen.getByLabelText('Sortcode'), '123456')
    await user.type(screen.getByLabelText('Account Number'), '00000001')
    await user.click(screen.getByRole('button', { name: 'Inquire Account' }))

    expect(fetchSpy).toHaveBeenCalledWith(
      '/v1/accounts/123456/00000001',
      expect.objectContaining({
        headers: expect.objectContaining({ Authorization: 'Bearer valid-inqacc-inquirer-token' }),
      }),
    )

    expect(await screen.findByText('Account inquiry successful.')).toBeInTheDocument()
    expect(screen.getByText('Eyecatcher')).toBeInTheDocument()
    expect(screen.getByText('Customer Number')).toBeInTheDocument()
    expect(screen.getAllByText('Sortcode').length).toBeGreaterThan(0)
    expect(screen.getAllByText('Account Number').length).toBeGreaterThan(0)
    expect(screen.getByText('Account Type')).toBeInTheDocument()
    expect(screen.getByText('Interest Rate')).toBeInTheDocument()
    expect(screen.getByText('Account Opened')).toBeInTheDocument()
    expect(screen.getByText('Overdraft Limit')).toBeInTheDocument()
    expect(screen.getByText('Last Statement Date')).toBeInTheDocument()
    expect(screen.getByText('Next Statement Date')).toBeInTheDocument()
    expect(screen.getByText('Available Balance')).toBeInTheDocument()
    expect(screen.getByText('Actual Balance')).toBeInTheDocument()
    expect(screen.getByText('Correlation ID: corr-success')).toBeInTheDocument()
  })

  it('prevents duplicate submission while same request is loading', async () => {
    let resolveResponse: (value: Response) => void = () => {}
    const pendingResponse = new Promise<Response>((resolve) => {
      resolveResponse = resolve
    })

    const fetchSpy = vi.spyOn(globalThis, 'fetch').mockImplementation(() => pendingResponse)

    renderPage()
    const user = userEvent.setup()

    await user.type(screen.getByLabelText('Sortcode'), '123456')
    await user.type(screen.getByLabelText('Account Number'), '00000001')

    await user.click(screen.getByRole('button', { name: 'Inquire Account' }))
    await user.click(screen.getByRole('button', { name: 'Loading...' }))

    expect(fetchSpy).toHaveBeenCalledTimes(1)

    resolveResponse(
      new Response(JSON.stringify(accountSuccessBody()), {
        status: 200,
        headers: {
          'content-type': 'application/json',
          'X-Correlation-ID': 'corr-dup',
        },
      }),
    )

    await screen.findByText('Account inquiry successful.')
  })

  it('keeps latest response when requests finish out of order', async () => {
    let resolveFirst: (value: Response) => void = () => {}
    let resolveSecond: (value: Response) => void = () => {}

    const firstPromise = new Promise<Response>((resolve) => {
      resolveFirst = resolve
    })
    const secondPromise = new Promise<Response>((resolve) => {
      resolveSecond = resolve
    })

    let callIndex = 0
    vi.spyOn(globalThis, 'fetch').mockImplementation(() => {
      callIndex += 1
      return callIndex === 1 ? firstPromise : secondPromise
    })

    renderPage()
    const user = userEvent.setup()

    await user.type(screen.getByLabelText('Sortcode'), '123456')
    await user.type(screen.getByLabelText('Account Number'), '00000001')
    await user.click(screen.getByRole('button', { name: 'Inquire Account' }))

    await user.clear(screen.getByLabelText('Account Number'))
    await user.type(screen.getByLabelText('Account Number'), '00000099')
    await user.click(screen.getByRole('button', { name: 'Loading...' }))

    resolveSecond(
      new Response(JSON.stringify(accountSuccessBody({ accountNumber: '00000099' })), {
        status: 200,
        headers: {
          'content-type': 'application/json',
          'X-Correlation-ID': 'corr-second',
        },
      }),
    )

    await screen.findByText('Correlation ID: corr-second')

    resolveFirst(
      new Response(JSON.stringify(accountSuccessBody({ accountNumber: '00000001' })), {
        status: 200,
        headers: {
          'content-type': 'application/json',
          'X-Correlation-ID': 'corr-first',
        },
      }),
    )

    await waitFor(() => {
      expect(screen.getByText('Correlation ID: corr-second')).toBeInTheDocument()
    })
  })

  it('renders distinct not-found and auth-error states', async () => {
    const fetchSpy = vi.spyOn(globalThis, 'fetch')
    fetchSpy
      .mockResolvedValueOnce(
        new Response(
          JSON.stringify({
            error: {
              code: 'ERR-004',
              message: 'Account record not found',
              timestamp: '2026-01-01T00:00:00Z',
              correlationId: 'corr-404',
            },
          }),
          {
            status: 404,
            headers: {
              'content-type': 'application/json',
              'X-Correlation-ID': 'corr-404',
            },
          },
        ),
      )
      .mockResolvedValueOnce(
        new Response(
          JSON.stringify({
            error: {
              code: 'ERR-002',
              message: 'Unauthorized',
              timestamp: '2026-01-01T00:00:00Z',
              correlationId: 'corr-401',
            },
          }),
          {
            status: 401,
            headers: {
              'content-type': 'application/json',
              'X-Correlation-ID': 'corr-401',
            },
          },
        ),
      )

    renderPage()
    const user = userEvent.setup()

    await user.type(screen.getByLabelText('Sortcode'), '123456')
    await user.type(screen.getByLabelText('Account Number'), '00000123')
    await user.click(screen.getByRole('button', { name: 'Inquire Account' }))

    expect((await screen.findAllByText('Account record not found')).length).toBeGreaterThan(0)
    expect(screen.getByText('ERR-004')).toBeInTheDocument()

    await user.clear(screen.getByLabelText('Account Number'))
    await user.type(screen.getByLabelText('Account Number'), '00000001')
    await user.click(screen.getByRole('button', { name: 'Inquire Account' }))

    expect((await screen.findAllByText('Unauthorized')).length).toBeGreaterThan(0)
    expect(screen.getByText('ERR-002')).toBeInTheDocument()
  })

  it('shows timeout error and clears stale success data', async () => {
    const fetchSpy = vi
      .spyOn(globalThis, 'fetch')
      .mockResolvedValueOnce(
        new Response(JSON.stringify(accountSuccessBody()), {
          status: 200,
          headers: {
            'content-type': 'application/json',
            'X-Correlation-ID': 'corr-success',
          },
        }),
      )
      .mockRejectedValueOnce(new DOMException('Timed out', 'AbortError'))

    renderPage()
    const user = userEvent.setup()

    await user.type(screen.getByLabelText('Sortcode'), '123456')
    await user.type(screen.getByLabelText('Account Number'), '00000001')
    await user.click(screen.getByRole('button', { name: 'Inquire Account' }))
    await screen.findByText('Account inquiry successful.')

    await user.click(screen.getByRole('button', { name: 'Repeat Inquiry' }))

    expect(await screen.findByText('The account inquiry request timed out. Please retry.')).toBeInTheDocument()
    expect(screen.queryByRole('heading', { name: 'Account Result' })).not.toBeInTheDocument()
    expect(fetchSpy).toHaveBeenCalledTimes(2)
  })

  it('shows network error and clears stale success data', async () => {
    const fetchSpy = vi
      .spyOn(globalThis, 'fetch')
      .mockResolvedValueOnce(
        new Response(JSON.stringify(accountSuccessBody()), {
          status: 200,
          headers: {
            'content-type': 'application/json',
            'X-Correlation-ID': 'corr-success',
          },
        }),
      )
      .mockRejectedValueOnce(new Error('network down'))

    renderPage()
    const user = userEvent.setup()

    await user.type(screen.getByLabelText('Sortcode'), '123456')
    await user.type(screen.getByLabelText('Account Number'), '00000001')
    await user.click(screen.getByRole('button', { name: 'Inquire Account' }))
    await screen.findByText('Account inquiry successful.')

    await user.click(screen.getByRole('button', { name: 'Repeat Inquiry' }))

    expect(await screen.findByText('Network unavailable. Check connection and retry.')).toBeInTheDocument()
    expect(screen.queryByRole('heading', { name: 'Account Result' })).not.toBeInTheDocument()
    expect(fetchSpy).toHaveBeenCalledTimes(2)
  })

  it('shows fallback backend error for non-json error response', async () => {
    vi.spyOn(globalThis, 'fetch')
      .mockResolvedValueOnce(
        new Response(JSON.stringify(accountSuccessBody()), {
          status: 200,
          headers: {
            'content-type': 'application/json',
            'X-Correlation-ID': 'corr-success',
          },
        }),
      )
      .mockResolvedValueOnce(
        new Response('service unavailable', {
          status: 503,
          headers: {
            'content-type': 'text/plain',
            'X-Correlation-ID': 'corr-503',
          },
        }),
      )

    renderPage()
    const user = userEvent.setup()

    await user.type(screen.getByLabelText('Sortcode'), '123456')
    await user.type(screen.getByLabelText('Account Number'), '00000001')
    await user.click(screen.getByRole('button', { name: 'Inquire Account' }))
    await screen.findByText('Account inquiry successful.')

    await user.click(screen.getByRole('button', { name: 'Repeat Inquiry' }))

    expect((await screen.findAllByText('Unexpected internal failure')).length).toBeGreaterThan(0)
    expect(screen.getByText('ERR-006')).toBeInTheDocument()
    expect(screen.queryByRole('heading', { name: 'Account Result' })).not.toBeInTheDocument()
  })

  it('supports repeat inquiry without page reload', async () => {
    const fetchSpy = vi
      .spyOn(globalThis, 'fetch')
      .mockResolvedValue(
        new Response(JSON.stringify(accountSuccessBody({ accountNumber: '00000001' })), {
          status: 200,
          headers: {
            'content-type': 'application/json',
            'X-Correlation-ID': 'corr-repeat',
          },
        }),
      )

    renderPage()
    const user = userEvent.setup()

    await user.type(screen.getByLabelText('Sortcode'), '123456')
    await user.type(screen.getByLabelText('Account Number'), '00000001')
    await user.click(screen.getByRole('button', { name: 'Inquire Account' }))
    await screen.findByText('Account inquiry successful.')

    await user.click(screen.getByRole('button', { name: 'Repeat Inquiry' }))

    expect(fetchSpy).toHaveBeenCalledTimes(2)
  })
})
