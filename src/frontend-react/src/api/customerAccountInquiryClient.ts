import { apiBaseUrl, requestTimeoutMs } from '../config/env'
import type {
  CustomerAccountError,
  CustomerAccountInquiryResponse,
  CustomerAccountInquiryResult,
} from '../domain/customerAccountTypes'

export async function inquireCustomerAccounts(customerNumber: string): Promise<CustomerAccountInquiryResult> {
  const controller = new AbortController()
  const timeout = setTimeout(() => controller.abort(), requestTimeoutMs)

  try {
    const response = await fetch(`${apiBaseUrl}/api/v1/customers/${customerNumber}/accounts`, {
      signal: controller.signal,
    })

    if (response.status === 200) {
      const data = (await response.json()) as CustomerAccountInquiryResponse
      return { type: 'success', status: 200, data }
    }

    if (response.status === 400 || response.status === 500) {
      const error = await safeParseError(response)
      return {
        type: 'backend-error',
        status: response.status,
        error,
      }
    }

    const fallback = await safeParseError(response)
    return {
      type: 'backend-error',
      status: 500,
      error: fallback,
    }
  } catch (error) {
    if (error instanceof DOMException && error.name === 'AbortError') {
      return { type: 'timeout', message: 'The request timed out. Please retry.' }
    }

    return { type: 'network-error', message: 'Network unavailable. Check connection and retry.' }
  } finally {
    clearTimeout(timeout)
  }
}

async function safeParseError(response: Response): Promise<CustomerAccountError> {
  try {
    return (await response.json()) as CustomerAccountError
  } catch {
    return {
      code: 'ERR-005',
      message: 'Internal processing error',
      details: [],
    }
  }
}
