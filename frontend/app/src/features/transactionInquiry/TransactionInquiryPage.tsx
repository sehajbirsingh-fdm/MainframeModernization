import { type FormEvent, useMemo, useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { inquireTransactions } from '../../api/transactionInquiryClient'
import type { TransactionInquiryRequest, TransactionInquiryResponse } from '../../domain/transactionTypes'
import { type TransactionInquiryValidationErrors, validateTransactionInquiryInput } from './validation'

interface TransactionInquiryForm {
  sortCode: string
  accountNumber: string
  fromDate: string
  toDate: string
  limit: string
  offset: string
}

function transactionKey(row: TransactionInquiryResponse['transactions'][number]): string {
  return row.transactionId
}

export function TransactionInquiryPage() {
  const [form, setForm] = useState<TransactionInquiryForm>({
    sortCode: '',
    accountNumber: '',
    fromDate: '',
    toDate: '',
    limit: '50',
    offset: '0',
  })
  const [validationErrors, setValidationErrors] = useState<TransactionInquiryValidationErrors>({})
  const [result, setResult] = useState<TransactionInquiryResponse | null>(null)
  const [message, setMessage] = useState('')
  const [errorCode, setErrorCode] = useState('')
  const [correlationId, setCorrelationId] = useState('')

  const mutation = useMutation({
    mutationFn: inquireTransactions,
    onSuccess: (response) => {
      if (response.type === 'success') {
        setResult(response.data)
        setErrorCode('')
        setCorrelationId(response.correlationId)
        if (response.data.returnedCount === 0) {
          setMessage('No transactions matched the supplied criteria.')
        } else {
          setMessage('Transaction inquiry successful.')
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

  const totalPages = useMemo(() => {
    if (!result || result.limit <= 0) {
      return 0
    }
    return Math.ceil(result.totalCount / result.limit)
  }, [result])

  function buildRequest(current: TransactionInquiryForm): TransactionInquiryRequest {
    return {
      sortCode: current.sortCode,
      accountNumber: current.accountNumber,
      fromDate: current.fromDate || undefined,
      toDate: current.toDate || undefined,
      limit: current.limit ? Number(current.limit) : undefined,
      offset: current.offset ? Number(current.offset) : undefined,
    }
  }

  function submitCurrentState(current: TransactionInquiryForm) {
    setValidationErrors({})
    setMessage('')
    setErrorCode('')
    mutation.mutate(buildRequest(current))
  }

  function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    const errors = validateTransactionInquiryInput(
      form.sortCode,
      form.accountNumber,
      form.fromDate,
      form.toDate,
      form.limit,
      form.offset,
    )

    if (Object.keys(errors).length > 0) {
      setValidationErrors(errors)
      setResult(null)
      setErrorCode('ERR-001')
      setCorrelationId('')
      setMessage('Please correct the highlighted fields and resubmit.')
      return
    }

    submitCurrentState(form)
  }

  function goToNextPage() {
    if (!result) {
      return
    }

    const nextOffset = result.offset + result.limit
    const nextForm = { ...form, offset: String(nextOffset) }
    setForm(nextForm)
    submitCurrentState(nextForm)
  }

  function goToPreviousPage() {
    if (!result) {
      return
    }

    const previousOffset = Math.max(0, result.offset - result.limit)
    const previousForm = { ...form, offset: String(previousOffset) }
    setForm(previousForm)
    submitCurrentState(previousForm)
  }

  return (
    <main className="page">
      <header className="page-header">
        <p className="pill">INQTRAN</p>
        <h1>Transaction Inquiry</h1>
        <p>Retrieve account transactions with optional date boundaries and paging controls.</p>
      </header>

      <section className="card" aria-labelledby="transaction-inquiry-form-heading">
        <h2 id="transaction-inquiry-form-heading">Inquiry Form</h2>
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
              onChange={(event) => setForm((previous) => ({ ...previous, sortCode: event.target.value.replace(/\D/g, '').slice(0, 6) }))}
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
            <label htmlFor="fromDate">From Date (optional)</label>
            <input
              id="fromDate"
              name="fromDate"
              type="text"
              maxLength={8}
              inputMode="numeric"
              value={form.fromDate}
              onChange={(event) => setForm((previous) => ({ ...previous, fromDate: event.target.value.replace(/\D/g, '').slice(0, 8) }))}
              aria-invalid={Boolean(validationErrors.fromDate)}
            />
            {validationErrors.fromDate ? <p className="error-text">{validationErrors.fromDate}</p> : null}
          </div>

          <div className="field">
            <label htmlFor="toDate">To Date (optional)</label>
            <input
              id="toDate"
              name="toDate"
              type="text"
              maxLength={8}
              inputMode="numeric"
              value={form.toDate}
              onChange={(event) => setForm((previous) => ({ ...previous, toDate: event.target.value.replace(/\D/g, '').slice(0, 8) }))}
              aria-invalid={Boolean(validationErrors.toDate)}
            />
            {validationErrors.toDate ? <p className="error-text">{validationErrors.toDate}</p> : null}
          </div>

          <div className="field">
            <label htmlFor="limit">Limit</label>
            <input
              id="limit"
              name="limit"
              type="text"
              inputMode="numeric"
              value={form.limit}
              onChange={(event) => setForm((previous) => ({ ...previous, limit: event.target.value.replace(/\D/g, '') }))}
              aria-invalid={Boolean(validationErrors.limit)}
            />
            {validationErrors.limit ? <p className="error-text">{validationErrors.limit}</p> : null}
          </div>

          <div className="field">
            <label htmlFor="offset">Offset</label>
            <input
              id="offset"
              name="offset"
              type="text"
              inputMode="numeric"
              value={form.offset}
              onChange={(event) => setForm((previous) => ({ ...previous, offset: event.target.value.replace(/\D/g, '') }))}
              aria-invalid={Boolean(validationErrors.offset)}
            />
            {validationErrors.offset ? <p className="error-text">{validationErrors.offset}</p> : null}
          </div>

          <div className="actions">
            <button type="submit" disabled={mutation.isPending}>{mutation.isPending ? 'Loading...' : 'Inquire Transactions'}</button>
          </div>
        </form>
      </section>

      <section className="status-region" aria-live="polite">
        {message ? <p className={errorCode ? 'error-text' : 'info-text'}>{message}</p> : null}
      </section>

      {result ? (
        <section className="card" aria-labelledby="transaction-meta-heading">
          <h2 id="transaction-meta-heading">Result Metadata</h2>
          <dl className="kv-grid">
            <dt>Sort Code</dt>
            <dd>{result.sortCode}</dd>
            <dt>Account Number</dt>
            <dd>{result.accountNumber}</dd>
            <dt>From Date</dt>
            <dd>{result.fromDate ?? '-'}</dd>
            <dt>To Date</dt>
            <dd>{result.toDate ?? '-'}</dd>
            <dt>Limit</dt>
            <dd>{result.limit}</dd>
            <dt>Offset</dt>
            <dd>{result.offset}</dd>
            <dt>Total Count</dt>
            <dd>{result.totalCount}</dd>
            <dt>Returned Count</dt>
            <dd>{result.returnedCount}</dd>
          </dl>

          <div className="actions">
            <button type="button" onClick={goToPreviousPage} disabled={mutation.isPending || result.offset === 0}>
              Previous Page
            </button>
            <button
              type="button"
              onClick={goToNextPage}
              disabled={mutation.isPending || result.offset + result.limit >= result.totalCount}
            >
              Next Page
            </button>
          </div>
          {totalPages > 0 ? <p className="info-text">Approx. total pages: {totalPages}</p> : null}
        </section>
      ) : null}

      {result ? (
        <section className="card" aria-labelledby="transaction-table-heading">
          <h2 id="transaction-table-heading">Transactions</h2>
          {result.transactions.length > 0 ? (
            <div className="table-wrap">
              <table className="accounts-table">
                <thead>
                  <tr>
                    <th>Transaction ID</th>
                    <th>Sort Code</th>
                    <th>Account Number</th>
                    <th>Date</th>
                    <th>Time</th>
                    <th>Reference</th>
                    <th>Type</th>
                    <th>Description</th>
                    <th>Amount</th>
                  </tr>
                </thead>
                <tbody>
                  {result.transactions.map((row) => (
                    <tr key={transactionKey(row)}>
                      <td>{row.transactionId}</td>
                      <td>{row.sortCode}</td>
                      <td>{row.accountNumber}</td>
                      <td>{row.date}</td>
                      <td>{row.time}</td>
                      <td>{row.reference}</td>
                      <td>{row.type}</td>
                      <td>{row.description}</td>
                      <td>{row.amount.toFixed(2)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <p className="warn-text">No transactions returned for this page.</p>
          )}
        </section>
      ) : null}

      {errorCode ? (
        <section className="card" aria-labelledby="transaction-error-heading">
          <h2 id="transaction-error-heading">Error Details</h2>
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
