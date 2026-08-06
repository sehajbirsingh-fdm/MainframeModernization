import { type FormEvent, useEffect, useState } from 'react'
import { useLocation, useNavigate, useParams } from 'react-router-dom'
import { updateCustomer } from '../../api/customerUpdateClient'
import { inquireCustomer } from '../../api/inquiryClient'
import type { CustomerInquiryResponse, CustomerResponse } from '../../domain/types'
import type {
  UpdateCustomerAddress,
  UpdateCustomerErrorEnvelope,
  UpdateCustomerRequest,
  UpdateCustomerResponse,
} from '../../domain/customerUpdateTypes'
import { validateCustomerUpdateInput, type CustomerUpdateValidationErrors } from './validation'

interface FormState {
  title: string
  firstName: string
  lastName: string
  dateOfBirth: string
  phoneNumber: string
  addressLine1: string
  addressLine2: string
  city: string
  postalCode: string
  country: string
  customerStatus: string
}

interface LocationState {
  customer?: CustomerResponse
}

const TITLE_OPTIONS = ['Mr', 'Mrs', 'Miss', 'Ms', 'Dr', 'Professor', 'Drs', 'Lord', 'Sir', 'Lady', '']

function mapCustomerToForm(customer: CustomerResponse | null | undefined): FormState {
  return {
    title: customer?.title ?? '',
    firstName: customer?.firstName ?? '',
    lastName: customer?.lastName ?? '',
    dateOfBirth: customer?.dateOfBirth ?? '',
    phoneNumber: customer?.phone ?? '',
    addressLine1: customer?.address?.line1 ?? '',
    addressLine2: customer?.address?.line2 ?? '',
    city: customer?.address?.city ?? '',
    postalCode: customer?.address?.postcode ?? '',
    country: customer?.address?.country ?? '',
    customerStatus: customer?.status ?? '',
  }
}

function toUpdateRequest(form: FormState): UpdateCustomerRequest {
  const address: UpdateCustomerAddress = {
    addressLine1: form.addressLine1,
    addressLine2: form.addressLine2,
    city: form.city,
    postalCode: form.postalCode,
    country: form.country,
  }

  return {
    title: form.title,
    firstName: form.firstName,
    lastName: form.lastName,
    dateOfBirth: form.dateOfBirth,
    phoneNumber: form.phoneNumber,
    address,
    customerStatus: form.customerStatus,
  }
}

function resolveUpdateErrorMessage(errorEnvelope: UpdateCustomerErrorEnvelope | null): string {
  const message = errorEnvelope?.error?.message
  if (typeof message === 'string' && message.trim().length > 0) {
    return message
  }
  return 'Update failed due to a system error. Please retry.'
}

function resolveUpdateErrorCode(errorEnvelope: UpdateCustomerErrorEnvelope | null): string {
  const code = errorEnvelope?.error?.code
  if (typeof code === 'string' && code.trim().length > 0) {
    return code
  }
  return 'UPDCUST-500-UNEXPECTED'
}

function resolveLegacyFailCode(errorEnvelope: UpdateCustomerErrorEnvelope | null): string {
  const legacyFailCode = errorEnvelope?.error?.legacyFailCode
  if (typeof legacyFailCode === 'string') {
    return legacyFailCode
  }
  return ' '
}

export function CustomerUpdatePage() {
  const params = useParams<{ sortCode: string; customerNumber: string }>()
  const location = useLocation()
  const navigate = useNavigate()
  const locationState = (location.state ?? {}) as LocationState

  const [form, setForm] = useState<FormState>(mapCustomerToForm(locationState.customer))
  const [validationErrors, setValidationErrors] = useState<CustomerUpdateValidationErrors>({})
  const [isLoading, setIsLoading] = useState(false)
  const [isPrefillLoading, setIsPrefillLoading] = useState(false)
  const [message, setMessage] = useState('')
  const [updated, setUpdated] = useState<UpdateCustomerResponse | null>(null)
  const [errorEnvelope, setErrorEnvelope] = useState<UpdateCustomerErrorEnvelope | null>(null)

  const sortCode = params.sortCode ?? ''
  const customerNumber = params.customerNumber ?? ''

  useEffect(() => {
    if (locationState.customer || !sortCode || !customerNumber) {
      return
    }

    let cancelled = false
    setIsPrefillLoading(true)

    void inquireCustomer({ sortCode, customerNumber }).then((result) => {
      if (cancelled) {
        return
      }

      if (result.type === 'success' && result.data.customer) {
        setForm(mapCustomerToForm(result.data.customer))
      } else {
        setMessage('Unable to prefill customer details. You can still edit manually.')
      }

      setIsPrefillLoading(false)
    })

    return () => {
      cancelled = true
    }
  }, [locationState.customer, sortCode, customerNumber])

  async function onSubmit(event: FormEvent<HTMLFormElement>): Promise<void> {
    event.preventDefault()

    const errors = validateCustomerUpdateInput(form)
    if (Object.keys(errors).length > 0) {
      setValidationErrors(errors)
      setUpdated(null)
      setErrorEnvelope(null)
      setMessage('Please correct the highlighted fields and resubmit.')
      return
    }

    if (!sortCode || !customerNumber) {
      setMessage('Missing route context for sort code or customer number.')
      return
    }

    setValidationErrors({})
    setIsLoading(true)
    setMessage('Updating customer...')
    setUpdated(null)
    setErrorEnvelope(null)

    const result = await updateCustomer(customerNumber, sortCode, toUpdateRequest(form))
    setIsLoading(false)

    if (result.type === 'success') {
      const inquiryResult: CustomerInquiryResponse = {
        legacyStatus: {
          inquirySuccess: 'Y',
          inquiryFailCode: ' ',
          message: 'Customer updated successfully.',
        },
        lookupMode: 'SPECIFIC',
        customer: {
          eyecatcher: locationState.customer?.eyecatcher ?? 'CUST',
          sortCode: result.data.sortCode,
          customerNumber: result.data.customerNumber,
          title: result.data.title,
          firstName: result.data.firstName,
          lastName: result.data.lastName,
          dateOfBirth: result.data.dateOfBirth,
          phone: result.data.phoneNumber,
          address: {
            line1: result.data.address.addressLine1,
            line2: result.data.address.addressLine2,
            city: result.data.address.city,
            postcode: result.data.address.postalCode,
            country: result.data.address.country,
          },
          status: result.data.customerStatus,
          createdDate: result.data.createdDate,
          creditScore: result.data.creditScore,
          creditScoreReviewDate: result.data.creditScoreReviewDate,
        },
        riskAssessment: null,
      }

      navigate('/customers', {
        replace: true,
        state: {
          inquiryResult,
          prefill: {
            sortCode: result.data.sortCode,
            customerNumber: result.data.customerNumber,
          },
          flashMessage: 'Customer updated successfully.',
        },
      })
      return
    }

    if (result.type === 'backend-error') {
      setErrorEnvelope(result.error)
      setMessage(resolveUpdateErrorMessage(result.error))
      return
    }

    setMessage(result.message)
  }

  return (
    <main className="page">
      <header className="page-header">
        <p className="pill">UPDCUST</p>
        <h1>Customer Update</h1>
        <p>Update customer details from inquiry context while preserving legacy status behavior.</p>
      </header>

      <section className="card" aria-labelledby="update-context-heading">
        <h2 id="update-context-heading">Customer Context</h2>
        <dl className="kv-grid">
          <dt>Sort Code</dt>
          <dd>{sortCode || '-'}</dd>
          <dt>Customer Number</dt>
          <dd>{customerNumber || '-'}</dd>
        </dl>
      </section>

      <section className="card" aria-labelledby="update-form-heading">
        <h2 id="update-form-heading">Update Form</h2>
        <form onSubmit={onSubmit} noValidate className="create-form">
          <div className="form-grid">
            <div className="field">
              <label htmlFor="title">Title</label>
              <select id="title" value={form.title} onChange={(e) => setForm((p) => ({ ...p, title: e.target.value }))}>
                {TITLE_OPTIONS.map((option) => (
                  <option key={option || 'blank'} value={option}>
                    {option || '(blank)'}
                  </option>
                ))}
              </select>
              {validationErrors.title ? <p className="error-text">{validationErrors.title}</p> : null}
            </div>

            <div className="field">
              <label htmlFor="firstName">First Name</label>
              <input id="firstName" type="text" maxLength={50} value={form.firstName} onChange={(e) => setForm((p) => ({ ...p, firstName: e.target.value }))} />
              {validationErrors.firstName ? <p className="error-text">{validationErrors.firstName}</p> : null}
            </div>

            <div className="field">
              <label htmlFor="lastName">Last Name</label>
              <input id="lastName" type="text" maxLength={50} value={form.lastName} onChange={(e) => setForm((p) => ({ ...p, lastName: e.target.value }))} />
              {validationErrors.lastName ? <p className="error-text">{validationErrors.lastName}</p> : null}
            </div>

            <div className="field">
              <label htmlFor="dateOfBirth">Date Of Birth (yyyy-MM-dd)</label>
              <input id="dateOfBirth" type="text" value={form.dateOfBirth} onChange={(e) => setForm((p) => ({ ...p, dateOfBirth: e.target.value }))} />
              {validationErrors.dateOfBirth ? <p className="error-text">{validationErrors.dateOfBirth}</p> : null}
            </div>

            <div className="field">
              <label htmlFor="phoneNumber">Phone Number</label>
              <input id="phoneNumber" type="text" maxLength={20} value={form.phoneNumber} onChange={(e) => setForm((p) => ({ ...p, phoneNumber: e.target.value }))} />
              {validationErrors.phoneNumber ? <p className="error-text">{validationErrors.phoneNumber}</p> : null}
            </div>

            <div className="field">
              <label htmlFor="customerStatus">Customer Status</label>
              <input id="customerStatus" type="text" maxLength={10} value={form.customerStatus} onChange={(e) => setForm((p) => ({ ...p, customerStatus: e.target.value }))} />
              {validationErrors.customerStatus ? <p className="error-text">{validationErrors.customerStatus}</p> : null}
            </div>

            <div className="field field-span-2">
              <label htmlFor="addressLine1">Address Line 1</label>
              <input id="addressLine1" type="text" maxLength={50} value={form.addressLine1} onChange={(e) => setForm((p) => ({ ...p, addressLine1: e.target.value }))} />
              {validationErrors.addressLine1 ? <p className="error-text">{validationErrors.addressLine1}</p> : null}
            </div>

            <div className="field field-span-2">
              <label htmlFor="addressLine2">Address Line 2</label>
              <input id="addressLine2" type="text" maxLength={50} value={form.addressLine2} onChange={(e) => setForm((p) => ({ ...p, addressLine2: e.target.value }))} />
              {validationErrors.addressLine2 ? <p className="error-text">{validationErrors.addressLine2}</p> : null}
            </div>

            <div className="field">
              <label htmlFor="city">City</label>
              <input id="city" type="text" maxLength={50} value={form.city} onChange={(e) => setForm((p) => ({ ...p, city: e.target.value }))} />
              {validationErrors.city ? <p className="error-text">{validationErrors.city}</p> : null}
            </div>

            <div className="field">
              <label htmlFor="postalCode">Postal Code</label>
              <input id="postalCode" type="text" maxLength={10} value={form.postalCode} onChange={(e) => setForm((p) => ({ ...p, postalCode: e.target.value }))} />
              {validationErrors.postalCode ? <p className="error-text">{validationErrors.postalCode}</p> : null}
            </div>

            <div className="field field-span-2">
              <label htmlFor="country">Country</label>
              <input id="country" type="text" maxLength={50} value={form.country} onChange={(e) => setForm((p) => ({ ...p, country: e.target.value }))} />
              {validationErrors.country ? <p className="error-text">{validationErrors.country}</p> : null}
            </div>
          </div>

          <p className="info-text form-note">Parity mode accepts any non-blank status value; production domain constraints are governed separately.</p>

          {validationErrors.payload ? <p className="error-text">{validationErrors.payload}</p> : null}

          <div className="actions">
            <button type="submit" disabled={isLoading || isPrefillLoading}>
              {isLoading ? 'Updating...' : 'Update Customer'}
            </button>
          </div>
        </form>
      </section>

      <section className="status-region" aria-live="polite">
        {message ? <p className={errorEnvelope ? 'error-text' : 'info-text'}>{message}</p> : null}
      </section>

      {updated ? (
        <section className="card" aria-labelledby="update-result-heading">
          <h2 id="update-result-heading">Update Result</h2>
          <dl className="kv-grid">
            <dt>Customer Number</dt>
            <dd>{updated.customerNumber}</dd>
            <dt>Sort Code</dt>
            <dd>{updated.sortCode}</dd>
            <dt>Status</dt>
            <dd>{updated.customerStatus}</dd>
            <dt>Legacy Success</dt>
            <dd>{updated.legacyStatus.updSuccess}</dd>
            <dt>Legacy Fail Code</dt>
            <dd>{updated.legacyStatus.updFailCode}</dd>
          </dl>
        </section>
      ) : null}

      {errorEnvelope ? (
        <section className="card" aria-labelledby="update-error-heading">
          <h2 id="update-error-heading">Error Details</h2>
          <dl className="kv-grid">
            <dt>Error Code</dt>
            <dd>{resolveUpdateErrorCode(errorEnvelope)}</dd>
            <dt>Legacy Fail Code</dt>
            <dd>{resolveLegacyFailCode(errorEnvelope)}</dd>
            <dt>Message</dt>
            <dd>{resolveUpdateErrorMessage(errorEnvelope)}</dd>
          </dl>
        </section>
      ) : null}
    </main>
  )
}
