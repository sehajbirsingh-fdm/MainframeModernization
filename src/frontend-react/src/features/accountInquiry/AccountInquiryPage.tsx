import { useMemo, useRef, useState } from 'react'
import type { FormEvent, ReactElement } from 'react'
import { inquireAccount } from '../../api/accountInquiryClient'
import { inqaccDefaultToken } from '../../config/env'
import type { AccountErrorEnvelope, AccountInquiryRequest, AccountResponse } from '../../domain/accountTypes'
import { type AccountValidationErrors, validateAccountInquiryInput } from './validation'

type UiState = 'IDLE' | 'LOADING' | 'SUCCESS' | 'NOT_FOUND' | 'ERROR'

interface AccountFormData {
  sortcode: string
  accountNumber: string
  bearerToken: string
}

function inputKey(form: AccountFormData): string {
  return `${form.sortcode}:${form.accountNumber}:${form.bearerToken}`
}

function fieldDisplay(label: string, value: string | number): ReactElement {
  return (
    <>
      <dt>{label}</dt>
      <dd>{value}</dd>
    </>
  )
}

function allAccountFields(data: AccountResponse): ReactElement {
  return (
    <dl className="kv-grid" aria-label="All account fields">
      {fieldDisplay('Eyecatcher', data.eyecatcher)}
      {fieldDisplay('Customer Number', data.customerNumber)}
      {fieldDisplay('Sortcode', data.sortcode)}
      {fieldDisplay('Account Number', data.accountNumber)}
      {fieldDisplay('Account Type', data.accountType)}
      {fieldDisplay('Interest Rate', data.interestRate)}
      {fieldDisplay('Account Opened', data.accountOpened)}
      {fieldDisplay('Overdraft Limit', data.overdraftLimit)}
      {fieldDisplay('Last Statement Date', data.lastStatementDate)}
      {fieldDisplay('Next Statement Date', data.nextStatementDate)}
      {fieldDisplay('Available Balance', data.availableBalance)}
      {fieldDisplay('Actual Balance', data.actualBalance)}
    </dl>
  )
}

export function AccountInquiryPage() {
  const [formData, setFormData] = useState<AccountFormData>({
    sortcode: '',
    accountNumber: '',
    bearerToken: inqaccDefaultToken,
  })
  const [validationErrors, setValidationErrors] = useState<AccountValidationErrors>({})
  const [uiState, setUiState] = useState<UiState>('IDLE')
  const [message, setMessage] = useState('')
  const [responseData, setResponseData] = useState<AccountResponse | null>(null)
  const [errorEnvelope, setErrorEnvelope] = useState<AccountErrorEnvelope | null>(null)
  const [correlationId, setCorrelationId] = useState('')

  const requestSequenceRef = useRef(0)
  const inFlightKeyRef = useRef('')

  const currentInputKey = useMemo(() => inputKey(formData), [formData])
  const isDuplicatePending = uiState === 'LOADING' && inFlightKeyRef.current === currentInputKey

  async function executeInquiry(request: AccountInquiryRequest): Promise<void> {
    const sequence = ++requestSequenceRef.current
    inFlightKeyRef.current = `${request.sortcode}:${request.accountNumber}:${request.bearerToken}`

    setUiState('LOADING')
    setMessage('Loading account inquiry...')

    const result = await inquireAccount(request)
    if (sequence !== requestSequenceRef.current) {
      return
    }

    if (result.type === 'success') {
      setUiState('SUCCESS')
      setResponseData(result.data)
      setErrorEnvelope(null)
      setCorrelationId(result.correlationId)
      setMessage('Account inquiry successful.')
      return
    }

    if (result.type === 'not-found') {
      setUiState('NOT_FOUND')
      setResponseData(null)
      setErrorEnvelope(result.error)
      setCorrelationId(result.correlationId || result.error.error.correlationId)
      setMessage(result.error.error.message)
      return
    }

    if (result.type === 'backend-error') {
      setUiState('ERROR')
      setResponseData(null)
      setErrorEnvelope(result.error)
      setCorrelationId(result.correlationId || result.error.error.correlationId)
      setMessage(result.error.error.message)
      return
    }

    setUiState('ERROR')
    setResponseData(null)
    setErrorEnvelope(null)
    setCorrelationId('')
    setMessage(result.message)
  }

  function onSubmit(event: FormEvent<HTMLFormElement>): void {
    event.preventDefault()

    if (isDuplicatePending) {
      return
    }

    const errors = validateAccountInquiryInput(formData.sortcode, formData.accountNumber)
    if (errors.sortcode || errors.accountNumber) {
      setValidationErrors(errors)
      setUiState('ERROR')
      setResponseData(null)
      setErrorEnvelope(null)
      setMessage('Please correct the highlighted fields and resubmit.')
      return
    }

    setValidationErrors({})
    void executeInquiry({
      sortcode: formData.sortcode,
      accountNumber: formData.accountNumber,
      bearerToken: formData.bearerToken,
    })
  }

  function repeatInquiry(): void {
    if (uiState === 'LOADING') {
      return
    }

    void executeInquiry({
      sortcode: formData.sortcode,
      accountNumber: formData.accountNumber,
      bearerToken: formData.bearerToken,
    })
  }

  return (
    <main className="page">
      <header className="page-header">
        <p className="pill">INQACC</p>
        <h1>Account Inquiry</h1>
        <p>Enter sortcode and account number. Use 99999999 to trigger highest-account lookup for the sortcode.</p>
      </header>

      <section className="card" aria-labelledby="account-inquiry-form-heading">
        <h2 id="account-inquiry-form-heading">Inquiry Form</h2>
        <form onSubmit={onSubmit} noValidate>
          <div className="field">
            <label htmlFor="sortcode">Sortcode</label>
            <input
              id="sortcode"
              name="sortcode"
              type="text"
              inputMode="numeric"
              maxLength={6}
              autoComplete="off"
              value={formData.sortcode}
              onChange={(event) =>
                setFormData((previous) => ({ ...previous, sortcode: event.target.value.replace(/\D/g, '').slice(0, 6) }))
              }
              aria-invalid={Boolean(validationErrors.sortcode)}
              aria-describedby={validationErrors.sortcode ? 'sortcode-error' : undefined}
            />
            {validationErrors.sortcode ? (
              <p id="sortcode-error" className="error-text">
                {validationErrors.sortcode}
              </p>
            ) : null}
          </div>

          <div className="field">
            <label htmlFor="accountNumber">Account Number</label>
            <input
              id="accountNumber"
              name="accountNumber"
              type="text"
              inputMode="numeric"
              maxLength={8}
              autoComplete="off"
              value={formData.accountNumber}
              onChange={(event) =>
                setFormData((previous) => ({
                  ...previous,
                  accountNumber: event.target.value.replace(/\D/g, '').slice(0, 8),
                }))
              }
              aria-invalid={Boolean(validationErrors.accountNumber)}
              aria-describedby={validationErrors.accountNumber ? 'accountNumber-error' : undefined}
            />
            {validationErrors.accountNumber ? (
              <p id="accountNumber-error" className="error-text">
                {validationErrors.accountNumber}
              </p>
            ) : null}
          </div>

          <div className="field">
            <label htmlFor="bearerToken">Bearer Token</label>
            <input
              id="bearerToken"
              name="bearerToken"
              type="text"
              autoComplete="off"
              value={formData.bearerToken}
              onChange={(event) => setFormData((previous) => ({ ...previous, bearerToken: event.target.value }))}
            />
          </div>

          <div className="actions">
            <button type="submit" disabled={isDuplicatePending}>
              {uiState === 'LOADING' ? 'Loading...' : 'Inquire Account'}
            </button>
            <button type="button" onClick={repeatInquiry} disabled={uiState === 'LOADING'}>
              Repeat Inquiry
            </button>
          </div>
        </form>
      </section>

      <section className="status-region" aria-live="polite">
        <p className={uiState === 'ERROR' ? 'error-text' : uiState === 'NOT_FOUND' ? 'warn-text' : 'info-text'}>{message}</p>
      </section>

      {uiState === 'SUCCESS' && responseData ? (
        <section className="card" aria-labelledby="account-response-heading">
          <h2 id="account-response-heading">Account Result</h2>
          {allAccountFields(responseData)}
        </section>
      ) : null}

      {errorEnvelope ? (
        <section className="card" aria-labelledby="account-error-heading">
          <h2 id="account-error-heading">Error Details</h2>
          <dl className="kv-grid">
            {fieldDisplay('Error Code', errorEnvelope.error.code)}
            {fieldDisplay('Message', errorEnvelope.error.message)}
            {fieldDisplay('Correlation ID', correlationId || errorEnvelope.error.correlationId)}
            {fieldDisplay('Timestamp', errorEnvelope.error.timestamp)}
          </dl>
        </section>
      ) : null}

      {!errorEnvelope && correlationId ? (
        <section className="card" aria-label="Correlation trace">
          <h2>Trace</h2>
          <p className="info-text">Correlation ID: {correlationId}</p>
        </section>
      ) : null}
    </main>
  )
}
