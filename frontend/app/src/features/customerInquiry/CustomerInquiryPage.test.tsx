import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { BrowserRouter, MemoryRouter, Route, Routes } from 'react-router-dom'
import { CustomerInquiryPage } from './CustomerInquiryPage'

function renderPage() {
  const queryClient = new QueryClient()

  return render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <CustomerInquiryPage />
      </BrowserRouter>
    </QueryClientProvider>,
  )
}

describe('CustomerInquiryPage', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('renders textbox-only inputs and no lookup dropdown', () => {
    renderPage()

    expect(screen.getByLabelText('Sort Code')).toBeInTheDocument()
    expect(screen.getByLabelText('Customer Number')).toBeInTheDocument()
    expect(screen.queryByRole('combobox')).not.toBeInTheDocument()
  })

  it('shows validation errors for invalid textbox input', async () => {
    renderPage()
    const user = userEvent.setup()

    await user.type(screen.getByLabelText('Sort Code'), '12')
    await user.type(screen.getByLabelText('Customer Number'), '123')
    await user.click(screen.getByRole('button', { name: 'Inquire' }))

    expect(screen.getByText('Sort code must be exactly 6 digits.')).toBeInTheDocument()
    expect(screen.getByText('Customer number must be exactly 10 digits.')).toBeInTheDocument()
  })

  it('calls backend and renders success using backend message', async () => {
    const fetchSpy = vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(
        JSON.stringify({
          legacyStatus: {
            inquirySuccess: 'Y',
            inquiryFailCode: '0',
            message: 'Inquiry successful',
          },
          lookupMode: 'SPECIFIC',
          customer: {
            eyecatcher: 'CUST',
            sortCode: '123456',
            customerNumber: '0000000001',
            title: 'Mr',
            firstName: 'John',
            lastName: 'Smith',
            dateOfBirth: '1975-01-01',
            phone: '4165550101',
            address: null,
            status: 'ACTIVE',
            createdDate: '2010-06-15',
            creditScore: 742,
            creditScoreReviewDate: '2026-01-15',
          },
          riskAssessment: {
            riskRating: 'LOW',
            reviewRequired: false,
            reasons: ['ACTIVE_SCORE_GE_700_REVIEW_CURRENT'],
          },
        }),
        { status: 200, headers: { 'content-type': 'application/json' } },
      ),
    )

    renderPage()
    const user = userEvent.setup()

    await user.type(screen.getByLabelText('Sort Code'), '123456')
    await user.type(screen.getByLabelText('Customer Number'), '0000000001')
    await user.click(screen.getByRole('button', { name: 'Inquire' }))

    expect(fetchSpy).toHaveBeenCalledWith(
      '/api/v1/customers/123456/0000000001',
      expect.objectContaining({ signal: expect.any(AbortSignal) }),
    )
    expect((await screen.findAllByText('Inquiry successful')).length).toBeGreaterThan(0)
    expect(screen.getByText('Inquiry Success')).toBeInTheDocument()
    expect(screen.getByText('Risk Assessment')).toBeInTheDocument()
    const updateLink = screen.getByRole('link', { name: 'Update Customer' })
    expect(updateLink).toBeInTheDocument()
    expect(updateLink).toHaveAttribute('href', '/customers/123456/0000000001/edit')
  })

  it('hydrates inquiry result from redirect state after update', async () => {
    const fetchSpy = vi.spyOn(globalThis, 'fetch')

    const queryClient = new QueryClient()
    render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter
          initialEntries={[
            {
              pathname: '/customers',
              state: {
                flashMessage: 'Customer updated successfully.',
                prefill: {
                  sortCode: '123456',
                  customerNumber: '0000000001',
                },
                inquiryResult: {
                  legacyStatus: {
                    inquirySuccess: 'Y',
                    inquiryFailCode: ' ',
                    message: 'Customer updated successfully.',
                  },
                  lookupMode: 'SPECIFIC',
                  customer: {
                    eyecatcher: 'CUST',
                    sortCode: '123456',
                    customerNumber: '0000000001',
                    title: 'Ms',
                    firstName: 'Jane',
                    lastName: 'Doe',
                    dateOfBirth: '1990-01-01',
                    phone: '4165550111',
                    address: {
                      line1: '10 Bay Street',
                      line2: 'Suite 200',
                      city: 'Toronto',
                      postcode: 'M5J2N8',
                      country: 'Canada',
                    },
                    status: 'SUSPENDED',
                    createdDate: '2010-06-15',
                    creditScore: 742,
                    creditScoreReviewDate: '2026-01-15',
                  },
                  riskAssessment: null,
                },
              },
            },
          ]}
        >
          <Routes>
            <Route path="/customers" element={<CustomerInquiryPage />} />
          </Routes>
        </MemoryRouter>
      </QueryClientProvider>,
    )

    expect((await screen.findAllByText('Customer updated successfully.')).length).toBeGreaterThan(0)
    expect(screen.getByText('Ms Jane Doe')).toBeInTheDocument()
    expect(screen.getByLabelText('Sort Code')).toHaveValue('123456')
    expect(screen.getByLabelText('Customer Number')).toHaveValue('0000000001')
    expect(fetchSpy).not.toHaveBeenCalled()
  })

  it('renders backend error message, code, and status for 400', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(
        JSON.stringify({
          errorCode: 'VALIDATION_ERROR',
          message: 'Request validation failed',
          fieldErrors: [{ field: 'sortCode', message: 'sortCode must match ^[0-9]{6}$' }],
        }),
        { status: 400, headers: { 'content-type': 'application/json' } },
      ),
    )

    renderPage()
    const user = userEvent.setup()

    await user.type(screen.getByLabelText('Sort Code'), '123456')
    await user.type(screen.getByLabelText('Customer Number'), '1234567890')
    await user.click(screen.getByRole('button', { name: 'Inquire' }))

    expect((await screen.findAllByText('Request validation failed')).length).toBeGreaterThan(0)
    expect(screen.getByText('Backend Error')).toBeInTheDocument()
    expect(screen.getByText('VALIDATION_ERROR')).toBeInTheDocument()
    expect(screen.getByText('400')).toBeInTheDocument()
    expect(screen.getByText('sortCode must match ^[0-9]{6}$')).toBeInTheDocument()
  })

  it('renders not-found response using backend legacy status message', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(
        JSON.stringify({
          legacyStatus: {
            inquirySuccess: 'N',
            inquiryFailCode: '1',
            message: 'Customer not found',
          },
          lookupMode: 'SPECIFIC',
          customer: null,
          riskAssessment: null,
        }),
        { status: 404, headers: { 'content-type': 'application/json' } },
      ),
    )

    renderPage()
    const user = userEvent.setup()

    await user.type(screen.getByLabelText('Sort Code'), '123456')
    await user.type(screen.getByLabelText('Customer Number'), '0000009999')
    await user.click(screen.getByRole('button', { name: 'Inquire' }))

    expect((await screen.findAllByText('Customer not found')).length).toBeGreaterThan(0)
    expect(screen.getByText('N')).toBeInTheDocument()
    expect(screen.getByText('1')).toBeInTheDocument()
  })

  it('renders backend error details for 500 response', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(
        JSON.stringify({
          errorCode: 'INTERNAL_ERROR',
          message: 'Unexpected system error',
          fieldErrors: [],
        }),
        { status: 500, headers: { 'content-type': 'application/json' } },
      ),
    )

    renderPage()
    const user = userEvent.setup()

    await user.type(screen.getByLabelText('Sort Code'), '123456')
    await user.type(screen.getByLabelText('Customer Number'), '1234567890')
    await user.click(screen.getByRole('button', { name: 'Inquire' }))

    expect((await screen.findAllByText('Unexpected system error')).length).toBeGreaterThan(0)
    expect(screen.getByText('INTERNAL_ERROR')).toBeInTheDocument()
    expect(screen.getByText('500')).toBeInTheDocument()
  })

  it('renders network error when backend is unavailable', async () => {
    vi.spyOn(globalThis, 'fetch').mockRejectedValue(new TypeError('Failed to fetch'))

    renderPage()
    const user = userEvent.setup()

    await user.type(screen.getByLabelText('Sort Code'), '123456')
    await user.type(screen.getByLabelText('Customer Number'), '1234567890')
    await user.click(screen.getByRole('button', { name: 'Inquire' }))

    expect(await screen.findByText('Network unavailable. Check connection and retry.')).toBeInTheDocument()
  })

  it('renders timeout message when request is aborted', async () => {
    vi.spyOn(globalThis, 'fetch').mockRejectedValue(new DOMException('The operation was aborted.', 'AbortError'))

    renderPage()
    const user = userEvent.setup()

    await user.type(screen.getByLabelText('Sort Code'), '123456')
    await user.type(screen.getByLabelText('Customer Number'), '1234567890')
    await user.click(screen.getByRole('button', { name: 'Inquire' }))

    expect(await screen.findByText('The request timed out. Please retry.')).toBeInTheDocument()
  })
})
