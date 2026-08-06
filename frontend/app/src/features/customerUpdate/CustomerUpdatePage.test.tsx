import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { CustomerUpdatePage } from './CustomerUpdatePage'
import { CustomerInquiryPage } from '../customerInquiry/CustomerInquiryPage'

function renderPage() {
  const queryClient = new QueryClient()

  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter
        initialEntries={[
          {
            pathname: '/customers/123456/0000000001/edit',
            state: {
              customer: {
                title: 'Mr',
                firstName: 'John',
                lastName: 'Smith',
                dateOfBirth: '1975-01-01',
                phone: '4165550101',
                address: {
                  line1: '1 Main Street',
                  line2: 'Suite 100',
                  city: 'Toronto',
                  postcode: 'M5H2N2',
                  country: 'Canada',
                },
                status: 'ACTIVE',
              },
            },
          },
        ]}
      >
        <Routes>
          <Route path="/customers/:sortCode/:customerNumber/edit" element={<CustomerUpdatePage />} />
          <Route path="/customers" element={<CustomerInquiryPage />} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

describe('CustomerUpdatePage', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('shows validation error when meaningful payload is missing', async () => {
    renderPage()
    const user = userEvent.setup()

    await user.clear(screen.getByLabelText('First Name'))
    await user.clear(screen.getByLabelText('Last Name'))
    await user.clear(screen.getByLabelText('Address Line 1'))
    await user.click(screen.getByRole('button', { name: 'Update Customer' }))

    expect(screen.getByText('At least one meaningful update is required in first name, last name, or address line 1.')).toBeInTheDocument()
  })

  it('submits update and returns to inquiry with updated customer details', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(
        JSON.stringify({
          customerNumber: '0000000001',
          sortCode: '123456',
          title: 'Ms',
          firstName: 'Jane',
          lastName: 'Doe',
          dateOfBirth: '1990-01-01',
          phoneNumber: '4165550111',
          address: {
            addressLine1: '10 Bay Street',
            addressLine2: 'Suite 200',
            city: 'Toronto',
            postalCode: 'M5J2N8',
            country: 'Canada',
          },
          customerStatus: 'SUSPENDED',
          createdDate: '2010-06-15',
          creditScore: 742,
          creditScoreReviewDate: '2026-01-15',
          legacyStatus: {
            updSuccess: 'Y',
            updFailCode: ' ',
          },
        }),
        { status: 200, headers: { 'content-type': 'application/json' } },
      ),
    )

    renderPage()
    const user = userEvent.setup()

    await user.selectOptions(screen.getByLabelText('Title'), 'Ms')
    await user.clear(screen.getByLabelText('First Name'))
    await user.type(screen.getByLabelText('First Name'), 'Jane')
    await user.clear(screen.getByLabelText('Last Name'))
    await user.type(screen.getByLabelText('Last Name'), 'Doe')
    await user.clear(screen.getByLabelText('Address Line 1'))
    await user.type(screen.getByLabelText('Address Line 1'), '10 Bay Street')
    await user.clear(screen.getByLabelText('Customer Status'))
    await user.type(screen.getByLabelText('Customer Status'), 'SUSPENDED')

    await user.click(screen.getByRole('button', { name: 'Update Customer' }))

    expect(await screen.findByText('Customer Inquiry')).toBeInTheDocument()
    expect((await screen.findAllByText('Customer updated successfully.')).length).toBeGreaterThan(0)
    expect(screen.getByText('Ms Jane Doe')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Update Customer' })).toHaveAttribute(
      'href',
      '/customers/123456/0000000001/edit',
    )
  })

  it('shows safe error details when backend returns non-standard 500 payload', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(
        JSON.stringify({
          timestamp: '2026-07-30T12:45:00Z',
          status: 500,
          error: 'Internal Server Error',
          path: '/api/v1/customers/0000000001',
        }),
        { status: 500, headers: { 'content-type': 'application/json' } },
      ),
    )

    renderPage()
    const user = userEvent.setup()

    await user.click(screen.getByRole('button', { name: 'Update Customer' }))

    expect(await screen.findByText('Customer Update')).toBeInTheDocument()
    expect(await screen.findByText('Error Details')).toBeInTheDocument()
    expect(screen.getByText('UPDCUST-500-UNEXPECTED')).toBeInTheDocument()
    expect(screen.getAllByText('Internal Server Error').length).toBeGreaterThan(0)
  })
})
