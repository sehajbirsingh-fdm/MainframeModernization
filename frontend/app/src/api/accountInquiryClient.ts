import { apiBaseUrl, inqaccDefaultToken, requestTimeoutMs } from '../config/env'
import type { AccountErrorEnvelope, AccountInquiryRequest, AccountInquiryResult, AccountResponse } from '../domain/accountTypes'

export async function inquireAccount(request: AccountInquiryRequest): Promise<AccountInquiryResult> {
  const controller = new AbortController()
  const timeout = setTimeout(() => controller.abort(), requestTimeoutMs)

  try {
    const response = await fetch(
      `${apiBaseUrl}/v1/accounts/${request.sortcode}/${request.accountNumber}`,
      {
        signal: controller.signal,
        headers: {
          Authorization: `Bearer ${inqaccDefaultToken}`,
        },
      },
    )

    const correlationId = response.headers.get('X-Correlation-ID') ?? ''

    if (response.status === 200) {
      const data = (await response.json()) as AccountResponse
      return {
        type: 'success',
        status: 200,
        data,
        correlationId,
      }
    }

    const errorEnvelope = await safeParseError(response, correlationId)

    if (response.status === 404) {
      return {
        type: 'not-found',
        status: 404,
        error: errorEnvelope,
        correlationId,
      }
    }

    if (response.status === 400 || response.status === 401 || response.status === 403 || response.status === 500 || response.status === 503) {
      return {
        type: 'backend-error',
        status: response.status,
        error: errorEnvelope,
        correlationId,
      }
    }

    return {
      type: 'backend-error',
      status: 500,
      error: errorEnvelope,
      correlationId,
    }
  } catch (error) {
    if (error instanceof DOMException && error.name === 'AbortError') {
      return { type: 'timeout', message: 'The account inquiry request timed out. Please retry.' }
    }

    return { type: 'network-error', message: 'Network unavailable. Check connection and retry.' }
  } finally {
    clearTimeout(timeout)
  }
}

async function safeParseError(response: Response, correlationId: string): Promise<AccountErrorEnvelope> {
  try {
    return (await response.json()) as AccountErrorEnvelope
  } catch {
    return {
      error: {
        code: 'ERR-006',
        message: 'Unexpected internal failure',
        timestamp: new Date().toISOString(),
        correlationId,
      },
    }
  }
}
