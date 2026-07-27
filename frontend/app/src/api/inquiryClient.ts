import { apiBaseUrl, requestTimeoutMs } from '../config/env'
import type { CustomerInquiryResponse, ErrorResponse } from '../domain/types'

export interface InquiryRequest {
  sortCode: string
  customerNumber: string
}

export type InquiryResult =
  | { type: 'success'; status: 200 | 404; data: CustomerInquiryResponse }
  | { type: 'backend-error'; status: number; error: ErrorResponse }
  | { type: 'timeout'; message: string }
  | { type: 'network-error'; message: string }

export async function inquireCustomer(request: InquiryRequest): Promise<InquiryResult> {
  return liveInquire(request)
}

async function liveInquire(request: InquiryRequest): Promise<InquiryResult> {
  const controller = new AbortController()
  const timeout = setTimeout(() => controller.abort(), requestTimeoutMs)

  try {
    const response = await fetch(
      `${apiBaseUrl}/api/v1/customers/${request.sortCode}/${request.customerNumber}`,
      { signal: controller.signal },
    )

    if (response.status === 200 || response.status === 404) {
      const data = (await response.json()) as CustomerInquiryResponse
      return { type: 'success', status: response.status, data }
    }

    const errorPayload = await safeParseError(response)
    return { type: 'backend-error', status: response.status, error: errorPayload }
  } catch (error) {
    if (error instanceof DOMException && error.name === 'AbortError') {
      return { type: 'timeout', message: 'The request timed out. Please retry.' }
    }

    return { type: 'network-error', message: 'Network unavailable. Check connection and retry.' }
  } finally {
    clearTimeout(timeout)
  }
}

async function safeParseError(response: Response): Promise<ErrorResponse> {
  try {
    return (await response.json()) as ErrorResponse
  } catch {
    return {
      errorCode: 'INTERNAL_ERROR',
      message: 'Unexpected system error',
      fieldErrors: [],
    }
  }
}
