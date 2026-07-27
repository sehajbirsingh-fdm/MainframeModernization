import { type FormEvent, useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { inquireCustomerAccounts } from '../../api/customerAccountInquiryClient'
import type {
  AccountSummary,
  CustomerAccountError,
  CustomerAccountInquiryResponse,
} from '../../domain/customerAccountTypes'
import { validateCustomerAccountInput } from './validation'

function accountRow(account: AccountSummary) {
  return (
    <tr key={`${account.sortCode}-${account.accountNumber}`}>
      <td>{account.eyecatcher}</td>
      <td>{account.customerNumber}</td>
      <td>{account.accountNumber}</td>
      <td>{account.sortCode}</td>
      <td>{account.accountType}</td>
      <td>{account.openedDate}</td>
      <td>{account.availableBalance.toFixed(2)}</td>
      <td>{account.actualBalance.toFixed(2)}</td>
      <td>{account.interestRate.toFixed(2)}</td>
      <td>{account.overdraftLimit}</td>
      <td>{account.lastStatementDate}</td>
      <td>{account.nextStatementDate}</td>
    </tr>
  )
}

export function CustomerAccountInquiryPage() {
  const [customerNumber, setCustomerNumber] = useState('')
  const [validationMessage, setValidationMessage] = useState<string | null>(null)
  const [result, setResult] = useState<CustomerAccountInquiryResponse | null>(null)
  const [error, setError] = useState<CustomerAccountError | null>(null)
  const [statusCode, setStatusCode] = useState<number | null>(null)
  const [message, setMessage] = useState('')

  const mutation = useMutation({
    mutationFn: inquireCustomerAccounts,
    onSuccess: (response) => {
      setValidationMessage(null)

      if (response.type === 'success') {
        setResult(response.data)
        setError(null)
        setStatusCode(200)

        if (response.data.legacyStatus.success === 'Y') {
          setMessage('Inquiry successful')
          return
        }

        if (response.data.legacyStatus.failCode === '1') {
          setMessage('Customer not found')
          return
        }

        if (['2', '3', '4'].includes(response.data.legacyStatus.failCode)) {
          setMessage('Account retrieval failed. Please retry later.')
          return
        }

        setMessage('Inquiry failed')
        return
      }

      setResult(null)

      if (response.type === 'backend-error') {
        setError(response.error)
        setStatusCode(response.status)
        setMessage(response.error.error.message)
        return
      }

      setError(null)
      setStatusCode(null)
      setMessage(response.message)
    },
  })

  function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    const errors = validateCustomerAccountInput(customerNumber)
    if (errors.customerNumber) {
      setValidationMessage(errors.customerNumber)
      setResult(null)
      setError(null)
      setStatusCode(400)
      setMessage('Please correct the highlighted fields and resubmit.')
      return
    }

    setValidationMessage(null)
    setMessage('')
    mutation.mutate(customerNumber)
  }

  const accounts = result?.accounts ?? []

  return (
    <main className="page">
      <header className="page-header">
        <p className="pill">INQACCCU</p>
        <h1>Customer Account Relationship Inquiry</h1>
        <p>Submit a customer number to retrieve the associated account relationships.</p>
      </header>

      <section className="card" aria-labelledby="customer-account-form-heading">
        <h2 id="customer-account-form-heading">Inquiry Form</h2>
        <form onSubmit={onSubmit} noValidate>
          <div className="field">
            <label htmlFor="customerNumber">Customer Number</label>
            <input
              id="customerNumber"
              name="customerNumber"
              type="text"
              inputMode="numeric"
              maxLength={10}
              value={customerNumber}
              onChange={(event) => setCustomerNumber(event.target.value.replace(/\D/g, '').slice(0, 10))}
              aria-invalid={Boolean(validationMessage)}
              aria-describedby={validationMessage ? 'customer-number-error' : undefined}
            />
            {validationMessage ? (
              <p id="customer-number-error" className="error-text">
                {validationMessage}
              </p>
            ) : null}
          </div>
          <div className="actions">
            <button type="submit" disabled={mutation.isPending}>{mutation.isPending ? 'Loading...' : 'Inquire'}</button>
          </div>
        </form>
      </section>

      <section className="status-region" aria-live="polite">
        {message ? <p className={error ? 'error-text' : 'info-text'}>{message}</p> : null}
      </section>

      {result ? (
        <section className="card" aria-labelledby="legacy-status-heading">
          <h2 id="legacy-status-heading">Legacy Status</h2>
          <dl className="kv-grid">
            <dt>success</dt>
            <dd>{result.legacyStatus.success}</dd>
            <dt>failCode</dt>
            <dd>{result.legacyStatus.failCode}</dd>
            <dt>customerFound</dt>
            <dd>{result.legacyStatus.customerFound}</dd>
          </dl>
        </section>
      ) : null}

      {result ? (
        <section className="card" aria-labelledby="customer-summary-heading">
          <h2 id="customer-summary-heading">Customer Summary</h2>
          <dl className="kv-grid">
            <dt>Customer Number</dt>
            <dd>{result.customerNumber}</dd>
          </dl>
        </section>
      ) : null}

      {result?.legacyStatus.success === 'Y' ? (
        <section className="card" aria-labelledby="accounts-heading">
          <h2 id="accounts-heading">Accounts ({result.numberOfAccounts})</h2>
          {accounts.length > 0 ? (
            <div className="table-wrap">
              <table className="accounts-table">
                <thead>
                  <tr>
                    <th>Eyecatcher</th>
                    <th>Customer Number</th>
                    <th>Account Number</th>
                    <th>Sort Code</th>
                    <th>Type</th>
                    <th>Opened Date</th>
                    <th>Available Balance</th>
                    <th>Actual Balance</th>
                    <th>Interest Rate</th>
                    <th>Overdraft Limit</th>
                    <th>Last Statement Date</th>
                    <th>Next Statement Date</th>
                  </tr>
                </thead>
                <tbody>{accounts.map(accountRow)}</tbody>
              </table>
            </div>
          ) : (
            <p className="warn-text">No accounts found for this customer.</p>
          )}
        </section>
      ) : null}

      {error ? (
        <section className="card" aria-labelledby="backend-error-heading">
          <h2 id="backend-error-heading">Backend Error</h2>
          <dl className="kv-grid">
            <dt>Status</dt>
            <dd>{statusCode ?? '-'}</dd>
            <dt>Type</dt>
            <dd>{error.error.type}</dd>
            <dt>Message</dt>
            <dd>{error.error.message}</dd>
          </dl>
        </section>
      ) : null}
    </main>
  )
}
