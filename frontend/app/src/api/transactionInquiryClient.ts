import { apiBaseUrl, requestTimeoutMs } from '../config/env'
import type { TransactionErrorResponse, TransactionInquiryRequest, TransactionInquiryResponse, TransactionInquiryResult } from '../domain/transactionTypes'

export async function inquireTransactions(request: TransactionInquiryRequest): Promise<TransactionInquiryResult> {
  const controller = new AbortController()
  const timeout = setTimeout(() => controller.abort(), requestTimeoutMs)

  try {
    const searchParams = new URLSearchParams()
    if (request.fromDate) {
      searchParams.set('fromDate', request.fromDate)
    }
    if (request.toDate) {
      searchParams.set('toDate', request.toDate)
    }
    if (request.limit !== undefined) {
      searchParams.set('limit', String(request.limit))
    }
    if (request.offset !== undefined) {
      searchParams.set('offset', String(request.offset))
    }

    const query = searchParams.toString()
    const url = `${apiBaseUrl}/api/v1/accounts/${request.sortCode}/${request.accountNumber}/transactions${query ? `?${query}` : ''}`

    const response = await fetch(url, { signal: controller.signal })
    const correlationId = response.headers.get('X-Correlation-ID') ?? ''

    if (response.status === 200) {
      const data = (await response.json()) as TransactionInquiryResponse
      return {
        type: 'success',
        status: 200,
        data,
        correlationId,
      }
    }

    if (response.status === 400 || response.status === 500) {
      const error = await safeParseError(response, correlationId)
      return {
        type: 'backend-error',
        status: response.status,
        error,
        correlationId,
      }
    }

    return {
      type: 'backend-error',
      status: 500,
      error: {
        code: 'ERR-500',
        message: 'Service unavailable due to infrastructure failure',
        correlationId,
      },
      correlationId,
    }
  } catch (error) {
    if (error instanceof DOMException && error.name === 'AbortError') {
      return { type: 'timeout', message: 'The transaction inquiry request timed out. Please retry.' }
    }

    return { type: 'network-error', message: 'Network unavailable. Check connection and retry.' }
  } finally {
    clearTimeout(timeout)
  }
}

async function safeParseError(response: Response, correlationId: string): Promise<TransactionErrorResponse> {
  try {
    return (await response.json()) as TransactionErrorResponse
  } catch {
    return {
      code: 'ERR-500',
      message: 'Service unavailable due to infrastructure failure',
      correlationId,
    }
  }
}
