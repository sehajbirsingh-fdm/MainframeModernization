import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { BrowserRouter } from 'react-router-dom'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { CustomerCreatePage } from './CustomerCreatePage'

function renderPage() {
  return render(
    <BrowserRouter>
      <CustomerCreatePage />
    </BrowserRouter>,
  )
}

describe('CustomerCreatePage', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('shows validation errors when required fields are missing', async () => {
    renderPage()
    const user = userEvent.setup()

    await user.clear(screen.getByLabelText('First Name'))
    await user.clear(screen.getByLabelText('Last Name'))
    await user.click(screen.getByRole('button', { name: 'Create Customer' }))

    expect(screen.getByText('Please correct the highlighted fields and resubmit.')).toBeInTheDocument()
  })

  it('submits and renders created customer result', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(
        JSON.stringify({
          eyecatcher: 'CUST',
          sortCode: '123456',
          customerNumber: '0000000006',
          title: 'Mr',
          firstName: 'John',
          lastName: 'Smith',
          dateOfBirth: '1990-01-01',
          phone: '4165550101',
          addressLine1: '1 Main',
          addressLine2: '',
          city: 'Toronto',
          postcode: 'M5H2N2',
          country: 'Canada',
          status: 'ACTIVE',
          createdDate: '2026-07-22',
          creditScore: 712,
          creditScoreReviewDate: '2026-08-05',
          legacyStatus: { commSuccess: 'Y', commFailCode: ' ' },
        }),
        {
          status: 201,
          headers: {
            'content-type': 'application/json',
            'X-Correlation-ID': 'corr-create-1',
          },
        },
      ),
    )

    renderPage()
    const user = userEvent.setup()

    await user.type(screen.getByLabelText('First Name'), 'John')
    await user.type(screen.getByLabelText('Last Name'), 'Smith')
    await user.type(screen.getByLabelText('DOB day'), '01')
    await user.type(screen.getByLabelText('DOB month'), '01')
    await user.type(screen.getByLabelText('DOB year'), '1990')
    await user.type(screen.getByLabelText('Phone'), '4165550101')
    await user.type(screen.getByLabelText('Address Line 1'), '1 Main')
    await user.type(screen.getByLabelText('City'), 'Toronto')
    await user.type(screen.getByLabelText('Postcode'), 'M5H2N2')
    await user.type(screen.getByLabelText('Country'), 'Canada')

    await user.click(screen.getByRole('button', { name: 'Create Customer' }))

    expect(await screen.findByText('Customer created successfully.')).toBeInTheDocument()
    expect(screen.getByText('0000000006')).toBeInTheDocument()
    expect(screen.getByText('corr-create-1')).toBeInTheDocument()
  })
})
