import { apiBaseUrl, requestTimeoutMs } from '../config/env'
import type {
  CreateCustomerErrorEnvelope,
  CreateCustomerRequest,
  CreateCustomerResponse,
  CreateCustomerResult,
} from '../domain/customerCreateTypes'

export async function createCustomer(request: CreateCustomerRequest): Promise<CreateCustomerResult> {
  const controller = new AbortController()
  const timeout = setTimeout(() => controller.abort(), requestTimeoutMs)

  try {
    const response = await fetch(`${apiBaseUrl}/v1/customers`, {
      method: 'POST',
      signal: controller.signal,
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(request),
    })

    const correlationId = response.headers.get('X-Correlation-ID') ?? ''

    if (response.status === 201) {
      const data = (await response.json()) as CreateCustomerResponse
      return {
        type: 'success',
        status: 201,
        data,
        correlationId,
      }
    }

    const errorEnvelope = await safeParseError(response, correlationId)

    if (response.status === 400 || response.status === 422 || response.status === 500 || response.status === 503) {
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
      return { type: 'timeout', message: 'The customer create request timed out. Please retry.' }
    }

    return { type: 'network-error', message: 'Network unavailable. Check connection and retry.' }
  } finally {
    clearTimeout(timeout)
  }
}

async function safeParseError(response: Response, correlationId: string): Promise<CreateCustomerErrorEnvelope> {
  try {
    return (await response.json()) as CreateCustomerErrorEnvelope
  } catch {
    return {
      error: {
        code: 'ERR-999',
        message: 'Unexpected internal failure',
        correlationId,
        timestamp: new Date().toISOString(),
      },
    }
  }
}
