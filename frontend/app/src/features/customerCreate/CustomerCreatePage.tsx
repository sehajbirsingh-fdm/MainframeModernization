import { useState } from 'react'
import type { FormEvent } from 'react'
import { createCustomer } from '../../api/customerCreateClient'
import type { CreateCustomerErrorEnvelope, CreateCustomerRequest, CreateCustomerResponse } from '../../domain/customerCreateTypes'
import { validateCustomerCreateInput, type CustomerCreateValidationErrors } from './validation'

interface FormState {
  title: string
  firstName: string
  lastName: string
  dobDay: string
  dobMonth: string
  dobYear: string
  createdDay: string
  createdMonth: string
  createdYear: string
  phone: string
  addressLine1: string
  addressLine2: string
  city: string
  postcode: string
  country: string
  status: string
}

const TITLE_OPTIONS = ['Mr', 'Mrs', 'Miss', 'Ms', 'Dr', 'Professor', 'Drs', 'Lord', 'Sir', 'Lady']

function initialFormState(): FormState {
  const now = new Date()
  return {
    title: 'Mr',
    firstName: '',
    lastName: '',
    dobDay: '',
    dobMonth: '',
    dobYear: '',
    createdDay: String(now.getDate()).padStart(2, '0'),
    createdMonth: String(now.getMonth() + 1).padStart(2, '0'),
    createdYear: String(now.getFullYear()),
    phone: '',
    addressLine1: '',
    addressLine2: '',
    city: '',
    postcode: '',
    country: '',
    status: 'ACTIVE',
  }
}

function toRequestPayload(form: FormState): CreateCustomerRequest {
  return {
    title: form.title,
    firstName: form.firstName,
    lastName: form.lastName,
    dateOfBirth: {
      day: Number(form.dobDay),
      month: Number(form.dobMonth),
      year: Number(form.dobYear),
    },
    createdDate: {
      day: Number(form.createdDay),
      month: Number(form.createdMonth),
      year: Number(form.createdYear),
    },
    phone: form.phone,
    address: {
      line1: form.addressLine1,
      line2: form.addressLine2,
      city: form.city,
      postcode: form.postcode,
      country: form.country,
    },
    status: form.status,
  }
}

export function CustomerCreatePage() {
  const [form, setForm] = useState<FormState>(initialFormState)
  const [validationErrors, setValidationErrors] = useState<CustomerCreateValidationErrors>({})
  const [isLoading, setIsLoading] = useState(false)
  const [message, setMessage] = useState('')
  const [created, setCreated] = useState<CreateCustomerResponse | null>(null)
  const [errorEnvelope, setErrorEnvelope] = useState<CreateCustomerErrorEnvelope | null>(null)
  const [correlationId, setCorrelationId] = useState('')

  async function onSubmit(event: FormEvent<HTMLFormElement>): Promise<void> {
    event.preventDefault()

    const errors = validateCustomerCreateInput(form)
    if (Object.keys(errors).length > 0) {
      setValidationErrors(errors)
      setCreated(null)
      setErrorEnvelope(null)
      setMessage('Please correct the highlighted fields and resubmit.')
      return
    }

    setValidationErrors({})
    setIsLoading(true)
    setMessage('Creating customer...')
    setCreated(null)
    setErrorEnvelope(null)

    const result = await createCustomer(toRequestPayload(form))
    setIsLoading(false)

    if (result.type === 'success') {
      setCreated(result.data)
      setCorrelationId(result.correlationId)
      setMessage('Customer created successfully.')
      return
    }

    if (result.type === 'backend-error') {
      setErrorEnvelope(result.error)
      setCorrelationId(result.correlationId || result.error.error.correlationId)
      setMessage(result.error.error.message)
      return
    }

    setMessage(result.message)
  }

  return (
    <main className="page">
      <header className="page-header">
        <p className="pill">CRECUST</p>
        <h1>Customer Create</h1>
        <p>Create customer using legacy-aligned fields and validation.</p>
      </header>

      <section className="card" aria-labelledby="create-form-heading">
        <h2 id="create-form-heading">Create Form</h2>
        <form onSubmit={onSubmit} noValidate className="create-form">
          <div className="form-grid">
          <div className="field">
            <label htmlFor="title">Title</label>
            <select id="title" value={form.title} onChange={(e) => setForm((p) => ({ ...p, title: e.target.value }))}>
              {TITLE_OPTIONS.map((option) => (
                <option key={option} value={option}>
                  {option}
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

          <div className="field field-span-2">
            <label>Date Of Birth (DD/MM/YYYY)</label>
            <div className="date-grid">
              <input aria-label="DOB day" type="text" inputMode="numeric" maxLength={2} value={form.dobDay} onChange={(e) => setForm((p) => ({ ...p, dobDay: e.target.value.replace(/\D/g, '').slice(0, 2) }))} />
              <input aria-label="DOB month" type="text" inputMode="numeric" maxLength={2} value={form.dobMonth} onChange={(e) => setForm((p) => ({ ...p, dobMonth: e.target.value.replace(/\D/g, '').slice(0, 2) }))} />
              <input aria-label="DOB year" type="text" inputMode="numeric" maxLength={4} value={form.dobYear} onChange={(e) => setForm((p) => ({ ...p, dobYear: e.target.value.replace(/\D/g, '').slice(0, 4) }))} />
            </div>
            {validationErrors.dateOfBirth ? <p className="error-text">{validationErrors.dateOfBirth}</p> : null}
          </div>

          <div className="field field-span-2">
            <label>Created Date (DD/MM/YYYY)</label>
            <div className="date-grid">
              <input aria-label="Created day" type="text" inputMode="numeric" maxLength={2} value={form.createdDay} onChange={(e) => setForm((p) => ({ ...p, createdDay: e.target.value.replace(/\D/g, '').slice(0, 2) }))} />
              <input aria-label="Created month" type="text" inputMode="numeric" maxLength={2} value={form.createdMonth} onChange={(e) => setForm((p) => ({ ...p, createdMonth: e.target.value.replace(/\D/g, '').slice(0, 2) }))} />
              <input aria-label="Created year" type="text" inputMode="numeric" maxLength={4} value={form.createdYear} onChange={(e) => setForm((p) => ({ ...p, createdYear: e.target.value.replace(/\D/g, '').slice(0, 4) }))} />
            </div>
            {validationErrors.createdDate ? <p className="error-text">{validationErrors.createdDate}</p> : null}
          </div>

          <div className="field">
            <label htmlFor="phone">Phone</label>
            <input id="phone" type="text" maxLength={20} value={form.phone} onChange={(e) => setForm((p) => ({ ...p, phone: e.target.value }))} />
            {validationErrors.phone ? <p className="error-text">{validationErrors.phone}</p> : null}
          </div>

          <div className="field">
            <label htmlFor="addressLine1">Address Line 1</label>
            <input id="addressLine1" type="text" maxLength={50} value={form.addressLine1} onChange={(e) => setForm((p) => ({ ...p, addressLine1: e.target.value }))} />
            {validationErrors.addressLine1 ? <p className="error-text">{validationErrors.addressLine1}</p> : null}
          </div>

          <div className="field">
            <label htmlFor="addressLine2">Address Line 2</label>
            <input id="addressLine2" type="text" maxLength={50} value={form.addressLine2} onChange={(e) => setForm((p) => ({ ...p, addressLine2: e.target.value }))} />
          </div>

          <div className="field">
            <label htmlFor="city">City</label>
            <input id="city" type="text" maxLength={50} value={form.city} onChange={(e) => setForm((p) => ({ ...p, city: e.target.value }))} />
            {validationErrors.city ? <p className="error-text">{validationErrors.city}</p> : null}
          </div>

          <div className="field">
            <label htmlFor="postcode">Postcode</label>
            <input id="postcode" type="text" maxLength={10} value={form.postcode} onChange={(e) => setForm((p) => ({ ...p, postcode: e.target.value }))} />
            {validationErrors.postcode ? <p className="error-text">{validationErrors.postcode}</p> : null}
          </div>

          <div className="field">
            <label htmlFor="country">Country</label>
            <input id="country" type="text" maxLength={50} value={form.country} onChange={(e) => setForm((p) => ({ ...p, country: e.target.value }))} />
            {validationErrors.country ? <p className="error-text">{validationErrors.country}</p> : null}
          </div>

          <div className="field">
            <label htmlFor="status">Status</label>
            <input id="status" type="text" maxLength={10} value={form.status} onChange={(e) => setForm((p) => ({ ...p, status: e.target.value }))} />
            {validationErrors.status ? <p className="error-text">{validationErrors.status}</p> : null}
          </div>
          </div>

          <div className="actions">
            <button type="submit" disabled={isLoading}>{isLoading ? 'Creating...' : 'Create Customer'}</button>
          </div>
        </form>
      </section>

      <section className="status-region" aria-live="polite">
        {message ? <p className={errorEnvelope ? 'error-text' : 'info-text'}>{message}</p> : null}
      </section>

      {created ? (
        <section className="card" aria-labelledby="create-result-heading">
          <h2 id="create-result-heading">Create Result</h2>
          <dl className="kv-grid">
            <dt>Customer Number</dt>
            <dd>{created.customerNumber}</dd>
            <dt>Sort Code</dt>
            <dd>{created.sortCode}</dd>
            <dt>Credit Score</dt>
            <dd>{created.creditScore}</dd>
            <dt>Review Date</dt>
            <dd>{created.creditScoreReviewDate}</dd>
            <dt>Legacy Success</dt>
            <dd>{created.legacyStatus.commSuccess}</dd>
            <dt>Legacy Fail Code</dt>
            <dd>{created.legacyStatus.commFailCode}</dd>
            <dt>Correlation ID</dt>
            <dd>{correlationId}</dd>
          </dl>
        </section>
      ) : null}

      {errorEnvelope ? (
        <section className="card" aria-labelledby="create-error-heading">
          <h2 id="create-error-heading">Error Details</h2>
          <dl className="kv-grid">
            <dt>Error Code</dt>
            <dd>{errorEnvelope.error.code}</dd>
            <dt>Legacy Fail Code</dt>
            <dd>{errorEnvelope.error.legacyFailCode ?? ' '}</dd>
            <dt>Correlation ID</dt>
            <dd>{correlationId || errorEnvelope.error.correlationId}</dd>
          </dl>
        </section>
      ) : null}
    </main>
  )
}
