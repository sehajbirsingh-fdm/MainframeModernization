import { apiBaseUrl, requestTimeoutMs } from '../config/env'
import type {
  UpdateCustomerErrorEnvelope,
  UpdateCustomerRequest,
  UpdateCustomerResponse,
  UpdateCustomerResult,
} from '../domain/customerUpdateTypes'

export async function updateCustomer(
  customerNumber: string,
  sortCode: string,
  request: UpdateCustomerRequest,
): Promise<UpdateCustomerResult> {
  const controller = new AbortController()
  const timeout = setTimeout(() => controller.abort(), requestTimeoutMs)

  try {
    const response = await fetch(
      `${apiBaseUrl}/api/v1/customers/${customerNumber}?sortCode=${encodeURIComponent(sortCode)}`,
      {
        method: 'PUT',
        signal: controller.signal,
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(request),
      },
    )

    const correlationId = response.headers.get('X-Correlation-ID') ?? ''

    if (response.status === 200) {
      const data = (await response.json()) as UpdateCustomerResponse
      return {
        type: 'success',
        status: 200,
        data,
        correlationId,
      }
    }

    const errorEnvelope = await safeParseError(response, correlationId)

    if (response.status === 400 || response.status === 404 || response.status === 422 || response.status === 500) {
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
      return { type: 'timeout', message: 'The customer update request timed out. Please retry.' }
    }

    return { type: 'network-error', message: 'Network unavailable. Check connection and retry.' }
  } finally {
    clearTimeout(timeout)
  }
}

async function safeParseError(response: Response, correlationId: string): Promise<UpdateCustomerErrorEnvelope> {
  try {
    const payload = (await response.json()) as unknown
    return normalizeErrorEnvelope(payload, correlationId)
  } catch {
    return fallbackErrorEnvelope(correlationId)
  }
}

function normalizeErrorEnvelope(payload: unknown, fallbackCorrelationId: string): UpdateCustomerErrorEnvelope {
  if (!payload || typeof payload !== 'object') {
    return fallbackErrorEnvelope(fallbackCorrelationId)
  }

  const candidate = payload as Record<string, unknown>
  const nestedError = (candidate.error ?? {}) as Record<string, unknown>

  const code = toText(nestedError.code) ?? toText(candidate.errorCode) ?? 'UPDCUST-500-UNEXPECTED'
  const message =
    toText(nestedError.message) ??
    toText(candidate.message) ??
    toText(candidate.error) ??
    'Unexpected internal failure'
  const legacyFailCode = toText(nestedError.legacyFailCode)
  const correlationId = toText(nestedError.correlationId) ?? fallbackCorrelationId
  const timestamp = toText(nestedError.timestamp) ?? new Date().toISOString()

  return {
    error: {
      code,
      message,
      legacyFailCode,
      correlationId,
      timestamp,
    },
  }
}

function fallbackErrorEnvelope(correlationId: string): UpdateCustomerErrorEnvelope {
  return {
    error: {
      code: 'UPDCUST-500-UNEXPECTED',
      message: 'Unexpected internal failure',
      correlationId,
      timestamp: new Date().toISOString(),
    },
  }
}

function toText(value: unknown): string | undefined {
  return typeof value === 'string' && value.trim().length > 0 ? value : undefined
}
