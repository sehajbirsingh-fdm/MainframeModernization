import { type FormEvent, useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { inquireStatement } from '../../api/statementInquiryClient'
import type { StatementEntry, StatementResponse } from '../../domain/statementTypes'
import { type StatementInquiryValidationErrors, validateStatementInquiryInput } from './validation'

interface StatementInquiryForm {
  sortCode: string
  accountNumber: string
  period: string
}

function entryKey(entry: StatementEntry): string {
  return `${entry.date}-${entry.time}-${entry.reference}`
}

export function StatementInquiryPage() {
  const [form, setForm] = useState<StatementInquiryForm>({
    sortCode: '',
    accountNumber: '',
    period: '',
  })
  const [validationErrors, setValidationErrors] = useState<StatementInquiryValidationErrors>({})
  const [result, setResult] = useState<StatementResponse | null>(null)
  const [message, setMessage] = useState('')
  const [errorCode, setErrorCode] = useState('')
  const [correlationId, setCorrelationId] = useState('')

  const mutation = useMutation({
    mutationFn: inquireStatement,
    onSuccess: (response) => {
      if (response.type === 'success') {
        setResult(response.data)
        setErrorCode('')
        setCorrelationId(response.correlationId)
        if (response.data.entries.length === 0) {
          setMessage('No statement entries found for the selected period.')
        } else {
          setMessage('Statement retrieval successful.')
        }
        return
      }

      setResult(null)

      if (response.type === 'backend-error') {
        setErrorCode(response.error.code)
        setCorrelationId(response.correlationId || response.error.correlationId || '')
        setMessage(response.error.message)
        return
      }

      setErrorCode('')
      setCorrelationId('')
      setMessage(response.message)
    },
  })

  function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    const errors = validateStatementInquiryInput(form.sortCode, form.accountNumber, form.period)
    if (Object.keys(errors).length > 0) {
      setValidationErrors(errors)
      setResult(null)
      setErrorCode('ERR-001')
      setCorrelationId('')
      setMessage('Please correct the highlighted fields and resubmit.')
      return
    }

    setValidationErrors({})
    setMessage('')
    setErrorCode('')
    mutation.mutate({
      sortCode: form.sortCode,
      accountNumber: form.accountNumber,
      period: form.period,
    })
  }

  return (
    <main className="page">
      <header className="page-header">
        <p className="pill">INQSTMT</p>
        <h1>Statement Inquiry</h1>
        <p>Retrieve monthly statement summary and transaction entries for a selected account.</p>
      </header>

      <section className="card" aria-labelledby="statement-inquiry-form-heading">
        <h2 id="statement-inquiry-form-heading">Inquiry Form</h2>
        <form onSubmit={onSubmit} noValidate>
          <div className="field">
            <label htmlFor="sortCode">Sort Code</label>
            <input
              id="sortCode"
              name="sortCode"
              type="text"
              maxLength={6}
              inputMode="numeric"
              value={form.sortCode}
              onChange={(event) =>
                setForm((previous) => ({
                  ...previous,
                  sortCode: event.target.value.replace(/\D/g, '').slice(0, 6),
                }))
              }
              aria-invalid={Boolean(validationErrors.sortCode)}
            />
            {validationErrors.sortCode ? <p className="error-text">{validationErrors.sortCode}</p> : null}
          </div>

          <div className="field">
            <label htmlFor="accountNumber">Account Number</label>
            <input
              id="accountNumber"
              name="accountNumber"
              type="text"
              maxLength={8}
              inputMode="numeric"
              value={form.accountNumber}
              onChange={(event) =>
                setForm((previous) => ({
                  ...previous,
                  accountNumber: event.target.value.replace(/\D/g, '').slice(0, 8),
                }))
              }
              aria-invalid={Boolean(validationErrors.accountNumber)}
            />
            {validationErrors.accountNumber ? <p className="error-text">{validationErrors.accountNumber}</p> : null}
          </div>

          <div className="field">
            <label htmlFor="period">Statement Period (YYYYMM)</label>
            <input
              id="period"
              name="period"
              type="text"
              maxLength={6}
              inputMode="numeric"
              value={form.period}
              onChange={(event) =>
                setForm((previous) => ({
                  ...previous,
                  period: event.target.value.replace(/\D/g, '').slice(0, 6),
                }))
              }
              aria-invalid={Boolean(validationErrors.period)}
            />
            {validationErrors.period ? <p className="error-text">{validationErrors.period}</p> : null}
          </div>

          <div className="actions">
            <button type="submit" disabled={mutation.isPending}>{mutation.isPending ? 'Loading...' : 'Retrieve Statement'}</button>
          </div>
        </form>
      </section>

      <section className="status-region" aria-live="polite">
        {message ? <p className={errorCode ? 'error-text' : 'info-text'}>{message}</p> : null}
      </section>

      {result ? (
        <section className="card" aria-labelledby="statement-summary-heading">
          <h2 id="statement-summary-heading">Statement Summary</h2>
          <dl className="kv-grid">
            <dt>Sort Code</dt>
            <dd>{result.sortCode}</dd>
            <dt>Account Number</dt>
            <dd>{result.accountNumber}</dd>
            <dt>Period</dt>
            <dd>{result.period}</dd>
            <dt>Period From</dt>
            <dd>{result.summary.periodFrom}</dd>
            <dt>Period To</dt>
            <dd>{result.summary.periodTo}</dd>
            <dt>Opening Balance</dt>
            <dd>{result.summary.openingBalance.toFixed(2)}</dd>
            <dt>Total Credits</dt>
            <dd>{result.summary.totalCredits.toFixed(2)}</dd>
            <dt>Total Debits</dt>
            <dd>{result.summary.totalDebits.toFixed(2)}</dd>
            <dt>Closing Balance</dt>
            <dd>{result.summary.closingBalance.toFixed(2)}</dd>
            <dt>Transaction Count</dt>
            <dd>{result.summary.transactionCount}</dd>
          </dl>
        </section>
      ) : null}

      {result ? (
        <section className="card" aria-labelledby="statement-entries-heading">
          <h2 id="statement-entries-heading">Statement Entries</h2>
          {result.entries.length > 0 ? (
            <div className="table-wrap">
              <table className="accounts-table">
                <thead>
                  <tr>
                    <th>Date</th>
                    <th>Time</th>
                    <th>Reference</th>
                    <th>Type</th>
                    <th>Description</th>
                    <th>Amount</th>
                  </tr>
                </thead>
                <tbody>
                  {result.entries.map((entry) => (
                    <tr key={entryKey(entry)}>
                      <td>{entry.date}</td>
                      <td>{entry.time}</td>
                      <td>{entry.reference}</td>
                      <td>{entry.type}</td>
                      <td>{entry.description}</td>
                      <td>{entry.amount.toFixed(2)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <p className="warn-text">No statement entries returned for this period.</p>
          )}
        </section>
      ) : null}

      {errorCode ? (
        <section className="card" aria-labelledby="statement-error-heading">
          <h2 id="statement-error-heading">Error Details</h2>
          <dl className="kv-grid">
            <dt>Error Code</dt>
            <dd>{errorCode}</dd>
            <dt>Correlation ID</dt>
            <dd>{correlationId || '-'}</dd>
          </dl>
        </section>
      ) : null}
    </main>
  )
}
