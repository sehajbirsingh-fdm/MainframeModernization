import { useEffect, useMemo, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { inquireTransactionDetail } from '../../api/transactionInquiryClient'
import { ACCOUNT_NUMBER_REGEX, DATE_REGEX, SORT_CODE_REGEX } from './validation'

const TIME_REGEX = /^\d{6}$/
const REFERENCE_REGEX = /^\d{12}$/

function isStructurallyValidIdentity(
  sortCode: string,
  accountNumber: string,
  date: string,
  time: string,
  reference: string,
): boolean {
  return (
    SORT_CODE_REGEX.test(sortCode)
    && ACCOUNT_NUMBER_REGEX.test(accountNumber)
    && DATE_REGEX.test(date)
    && TIME_REGEX.test(time)
    && REFERENCE_REGEX.test(reference)
  )
}

export function TransactionDetailPage() {
  const params = useParams<{
    sortCode: string
    accountNumber: string
    date: string
    time: string
    reference: string
  }>()

  const sortCode = params.sortCode ?? ''
  const accountNumber = params.accountNumber ?? ''
  const date = params.date ?? ''
  const time = params.time ?? ''
  const reference = params.reference ?? ''

  const [loading, setLoading] = useState(true)
  const [message, setMessage] = useState('')
  const [errorCode, setErrorCode] = useState('')
  const [correlationId, setCorrelationId] = useState('')
  const [found, setFound] = useState(false)
  const [transaction, setTransaction] = useState<{
    transactionId: string
    sortCode: string
    accountNumber: string
    date: string
    time: string
    reference: string
    type: string
    description: string
    amount: number
  } | null>(null)

  const validIdentity = useMemo(
    () => isStructurallyValidIdentity(sortCode, accountNumber, date, time, reference),
    [sortCode, accountNumber, date, time, reference],
  )

  useEffect(() => {
    let cancelled = false

    async function loadDetail() {
      if (!validIdentity) {
        setLoading(false)
        setErrorCode('ERR-001')
        setMessage('Transaction detail identity is structurally invalid.')
        setTransaction(null)
        setFound(false)
        setCorrelationId('')
        return
      }

      const result = await inquireTransactionDetail({ sortCode, accountNumber, date, time, reference })
      if (cancelled) {
        return
      }

      setLoading(false)

      if (result.type === 'success') {
        setErrorCode('')
        setCorrelationId(result.correlationId)
        setFound(result.data.found)
        setTransaction(result.data.transaction)
        if (result.data.found) {
          setMessage('Transaction detail retrieved successfully.')
        } else {
          setMessage('No transaction detail was found for the supplied identity.')
        }
        return
      }

      setFound(false)
      setTransaction(null)

      if (result.type === 'backend-error') {
        setErrorCode(result.error.code)
        setCorrelationId(result.correlationId || result.error.correlationId || '')
        setMessage(result.error.message)
        return
      }

      setErrorCode('')
      setCorrelationId('')
      setMessage(result.message)
    }

    loadDetail()

    return () => {
      cancelled = true
    }
  }, [sortCode, accountNumber, date, time, reference, validIdentity])

  return (
    <main className="page">
      <header className="page-header">
        <p className="pill">INQTRAND</p>
        <h1>Transaction Detail Inquiry</h1>
        <p>Read-only detail lookup by exact five-part identity.</p>
      </header>

      <section className="card" aria-labelledby="detail-identity-heading">
        <h2 id="detail-identity-heading">Identity</h2>
        <dl className="kv-grid">
          <dt>Sort Code</dt>
          <dd>{sortCode || '-'}</dd>
          <dt>Account Number</dt>
          <dd>{accountNumber || '-'}</dd>
          <dt>Date</dt>
          <dd>{date || '-'}</dd>
          <dt>Time</dt>
          <dd>{time || '-'}</dd>
          <dt>Reference</dt>
          <dd>{reference || '-'}</dd>
        </dl>
        <div className="actions">
          <Link to="/transactions" className="button-link">Back To Transaction Inquiry</Link>
        </div>
      </section>

      <section className="status-region" aria-live="polite">
        {loading ? <p className="info-text">Loading transaction detail...</p> : null}
        {!loading && message ? <p className={errorCode ? 'error-text' : found ? 'info-text' : 'warn-text'}>{message}</p> : null}
      </section>

      {!loading && found && transaction ? (
        <section className="card" aria-labelledby="detail-result-heading">
          <h2 id="detail-result-heading">Transaction Detail</h2>
          <dl className="kv-grid">
            <dt>Transaction ID</dt>
            <dd>{transaction.transactionId}</dd>
            <dt>Sort Code</dt>
            <dd>{transaction.sortCode}</dd>
            <dt>Account Number</dt>
            <dd>{transaction.accountNumber}</dd>
            <dt>Date</dt>
            <dd>{transaction.date}</dd>
            <dt>Time</dt>
            <dd>{transaction.time}</dd>
            <dt>Reference</dt>
            <dd>{transaction.reference}</dd>
            <dt>Type</dt>
            <dd>{transaction.type}</dd>
            <dt>Description</dt>
            <dd>{transaction.description}</dd>
            <dt>Amount</dt>
            <dd>{transaction.amount.toFixed(2)}</dd>
          </dl>
        </section>
      ) : null}

      {!loading && errorCode ? (
        <section className="card" aria-labelledby="detail-error-heading">
          <h2 id="detail-error-heading">Error Details</h2>
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
