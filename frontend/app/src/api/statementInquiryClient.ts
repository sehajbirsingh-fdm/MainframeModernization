import { apiBaseUrl, inqaccDefaultToken, requestTimeoutMs } from '../config/env'
import type { StatementErrorResponse, StatementInquiryResult, StatementRequest, StatementResponse } from '../domain/statementTypes'

export async function inquireStatement(request: StatementRequest): Promise<StatementInquiryResult> {
  const controller = new AbortController()
  const timeout = setTimeout(() => controller.abort(), requestTimeoutMs)

  try {
    const url = `${apiBaseUrl}/api/v1/accounts/${request.sortCode}/${request.accountNumber}/statements/${request.period}`
    const response = await fetch(url, {
      signal: controller.signal,
      headers: {
        Authorization: `Bearer ${inqaccDefaultToken}`,
      },
    })

    const correlationId = response.headers.get('X-Correlation-ID') ?? ''

    if (response.status === 200) {
      const data = (await response.json()) as StatementResponse
      return {
        type: 'success',
        status: 200,
        data,
        correlationId,
      }
    }

    if (response.status === 400 || response.status === 401 || response.status === 403 || response.status === 404 || response.status === 500) {
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
      return { type: 'timeout', message: 'The statement inquiry request timed out. Please retry.' }
    }

    return { type: 'network-error', message: 'Network unavailable. Check connection and retry.' }
  } finally {
    clearTimeout(timeout)
  }
}

async function safeParseError(response: Response, correlationId: string): Promise<StatementErrorResponse> {
  try {
    return (await response.json()) as StatementErrorResponse
  } catch {
    return {
      code: 'ERR-500',
      message: 'Service unavailable due to infrastructure failure',
      correlationId,
    }
  }
}
