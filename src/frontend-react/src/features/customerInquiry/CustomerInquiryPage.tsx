import { type FormEvent, useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { inquireCustomer } from '../../api/inquiryClient'
import type { CustomerInquiryResponse, ErrorResponse } from '../../domain/types'
import { validateInquiryInput, type ValidationErrors } from './validation'

interface FormData {
  sortCode: string
  customerNumber: string
}

function toValidationErrors(error: {
  fieldErrors?: Array<{ field: string; message: string }>
}): ValidationErrors {
  const mapped: ValidationErrors = {}
  for (const fieldError of error.fieldErrors ?? []) {
    if (fieldError.field === 'sortCode') {
      mapped.sortCode = fieldError.message
    }
    if (fieldError.field === 'customerNumber') {
      mapped.customerNumber = fieldError.message
    }
  }
  return mapped
}

function renderCustomerDetails(data: CustomerInquiryResponse) {
  if (!data.customer) {
    return null
  }

  return (
    <section aria-labelledby="customer-details-heading" className="card">
      <h2 id="customer-details-heading">Customer Details</h2>
      <dl className="kv-grid">
        <dt>Name</dt>
        <dd>{`${data.customer.title ?? ''} ${data.customer.firstName ?? ''} ${data.customer.lastName ?? ''}`.trim()}</dd>
        <dt>Customer Number</dt>
        <dd>{data.customer.customerNumber}</dd>
        <dt>Sort Code</dt>
        <dd>{data.customer.sortCode}</dd>
        <dt>Status</dt>
        <dd>{data.customer.status}</dd>
        <dt>Credit Score</dt>
        <dd>{data.customer.creditScore ?? 'N/A'}</dd>
      </dl>
    </section>
  )
}

function renderRisk(data: CustomerInquiryResponse) {
  if (!data.riskAssessment) {
    return null
  }

  return (
    <section aria-labelledby="risk-heading" className="card">
      <h2 id="risk-heading">Risk Assessment</h2>
      <dl className="kv-grid">
        <dt>Risk Rating</dt>
        <dd>{data.riskAssessment.riskRating}</dd>
        <dt>Review Required</dt>
        <dd>{String(data.riskAssessment.reviewRequired)}</dd>
      </dl>
      <h3>Reasons</h3>
      <ul>
        {data.riskAssessment.reasons.map((reason) => (
          <li key={reason}>{reason}</li>
        ))}
      </ul>
    </section>
  )
}

export function CustomerInquiryPage() {
  const [formData, setFormData] = useState<FormData>({ sortCode: '', customerNumber: '' })
  const [validationErrors, setValidationErrors] = useState<ValidationErrors>({})
  const [responseData, setResponseData] = useState<CustomerInquiryResponse | null>(null)
  const [uiMessage, setUiMessage] = useState<string>('')
  const [backendError, setBackendError] = useState<{ status: number; error: ErrorResponse } | null>(null)

  const mutation = useMutation({
    mutationFn: inquireCustomer,
    onSuccess: (result) => {
      if (result.type === 'success') {
        setValidationErrors({})
        setBackendError(null)
        setUiMessage(result.data.legacyStatus.message)
        setResponseData(result.data)
        return
      }

      if (result.type === 'backend-error') {
        setValidationErrors(toValidationErrors(result.error))
        setBackendError({ status: result.status, error: result.error })
        setUiMessage(result.error.message)
        setResponseData(null)
        return
      }

      setBackendError(null)
      setUiMessage(result.message)
      setResponseData(null)
    },
  })

  function onSubmit(event: FormEvent<HTMLFormElement>): void {
    event.preventDefault()

    const errors = validateInquiryInput(formData.sortCode, formData.customerNumber)
    if (errors.sortCode || errors.customerNumber) {
      setValidationErrors(errors)
      setBackendError(null)
      setResponseData(null)
      setUiMessage('Please correct the highlighted fields and resubmit.')
      return
    }

    setValidationErrors({})
    setBackendError(null)
    setUiMessage('')
    mutation.mutate(formData)
  }

  function retry(): void {
    mutation.mutate(formData)
  }

  const isLoading = mutation.isPending
  const isNotFound = responseData?.legacyStatus?.inquirySuccess === 'N'
  const isSystemMessage = Boolean(backendError || mutation.isError)

  return (
    <main className="page">
      <header className="page-header">
        <h1>Customer Inquiry</h1>
        <p>
          Enter sort code and customer number using text boxes only. Use 0000000000 for random and 9999999999 for latest lookup.
        </p>
      </header>

      <section className="card" aria-labelledby="inquiry-form-heading">
        <h2 id="inquiry-form-heading">Inquiry Form</h2>
        <form onSubmit={onSubmit} noValidate>
          <div className="field">
            <label htmlFor="sortCode">Sort Code</label>
            <input
              id="sortCode"
              name="sortCode"
              type="text"
              inputMode="numeric"
              pattern="[0-9]{6}"
              maxLength={6}
              autoComplete="off"
              value={formData.sortCode}
              onChange={(event) =>
                setFormData((prev) => ({ ...prev, sortCode: event.target.value.replace(/\D/g, '').slice(0, 6) }))
              }
              aria-invalid={Boolean(validationErrors.sortCode)}
              aria-describedby={validationErrors.sortCode ? 'sortCode-error' : undefined}
            />
            {validationErrors.sortCode ? (
              <p id="sortCode-error" className="error-text">
                {validationErrors.sortCode}
              </p>
            ) : null}
          </div>

          <div className="field">
            <label htmlFor="customerNumber">Customer Number</label>
            <input
              id="customerNumber"
              name="customerNumber"
              type="text"
              inputMode="numeric"
              pattern="[0-9]{10}"
              maxLength={10}
              autoComplete="off"
              value={formData.customerNumber}
              onChange={(event) =>
                setFormData((prev) => ({
                  ...prev,
                  customerNumber: event.target.value.replace(/\D/g, '').slice(0, 10),
                }))
              }
              aria-invalid={Boolean(validationErrors.customerNumber)}
              aria-describedby={validationErrors.customerNumber ? 'customerNumber-error' : undefined}
            />
            {validationErrors.customerNumber ? (
              <p id="customerNumber-error" className="error-text">
                {validationErrors.customerNumber}
              </p>
            ) : null}
          </div>

          <div className="actions">
            <button type="submit" disabled={isLoading}>
              {isLoading ? 'Loading...' : 'Inquire'}
            </button>
          </div>
        </form>
      </section>

      <section aria-live="polite" className="status-region">
        {uiMessage ? <p className={isSystemMessage ? 'error-text' : isNotFound ? 'warn-text' : 'info-text'}>{uiMessage}</p> : null}
        {mutation.isError ? <p className="error-text">Unexpected frontend error occurred.</p> : null}
      </section>

      {backendError ? (
        <section className="card" aria-labelledby="backend-error-heading">
          <h2 id="backend-error-heading">Backend Error</h2>
          <dl className="kv-grid">
            <dt>HTTP Status</dt>
            <dd>{backendError.status}</dd>
            <dt>Error Code</dt>
            <dd>{backendError.error.errorCode}</dd>
            <dt>Message</dt>
            <dd>{backendError.error.message}</dd>
          </dl>
          {backendError.error.fieldErrors && backendError.error.fieldErrors.length > 0 ? (
            <>
              <h3>Field Errors</h3>
              <ul>
                {backendError.error.fieldErrors.map((item) => (
                  <li key={`${item.field}:${item.message}`}>{`${item.field}: ${item.message}`}</li>
                ))}
              </ul>
            </>
          ) : null}
        </section>
      ) : null}

      {responseData ? (
        <section className="card" aria-labelledby="legacy-heading">
          <h2 id="legacy-heading">Legacy Status</h2>
          <dl className="kv-grid">
            <dt>Inquiry Success</dt>
            <dd>{responseData.legacyStatus.inquirySuccess}</dd>
            <dt>Inquiry Fail Code</dt>
            <dd>{responseData.legacyStatus.inquiryFailCode}</dd>
            <dt>Message</dt>
            <dd>{responseData.legacyStatus.message}</dd>
            <dt>Lookup Mode</dt>
            <dd>{responseData.lookupMode}</dd>
          </dl>
        </section>
      ) : null}

      {renderCustomerDetails(responseData ?? ({} as CustomerInquiryResponse))}
      {renderRisk(responseData ?? ({} as CustomerInquiryResponse))}

      {!responseData && uiMessage && !isLoading ? (
        <div className="actions">
          <button type="button" onClick={retry}>
            Retry
          </button>
        </div>
      ) : null}
    </main>
  )
}
