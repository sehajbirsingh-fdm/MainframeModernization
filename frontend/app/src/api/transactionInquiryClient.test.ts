import { afterEach, describe, expect, it, vi } from 'vitest'
import { inquireTransactionDetail, inquireTransactions } from './transactionInquiryClient'

describe('inquireTransactions', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('builds endpoint and omits optional dates when unsupplied', async () => {
    const fetchSpy = vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(
        JSON.stringify({
          sortCode: '123456',
          accountNumber: '00000001',
          fromDate: null,
          toDate: null,
          limit: 50,
          offset: 0,
          totalCount: 1,
          returnedCount: 1,
          transactions: [],
        }),
        {
          status: 200,
          headers: {
            'content-type': 'application/json',
            'X-Correlation-ID': 'corr-1',
          },
        },
      ),
    )

    const result = await inquireTransactions({
      sortCode: '123456',
      accountNumber: '00000001',
      limit: 50,
      offset: 0,
    })

    expect(fetchSpy).toHaveBeenCalledWith(
      '/api/v1/accounts/123456/00000001/transactions?limit=50&offset=0',
      expect.objectContaining({
        headers: expect.objectContaining({
          Authorization: 'Bearer valid-inqacc-inquirer-token',
        }),
      }),
    )
    expect(result.type).toBe('success')
  })

  it('returns backend error shape for 400 responses', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(
        JSON.stringify({
          code: 'ERR-001',
          message: 'Validation failed',
          correlationId: 'corr-400',
        }),
        {
          status: 400,
          headers: {
            'content-type': 'application/json',
            'X-Correlation-ID': 'corr-400',
          },
        },
      ),
    )

    const result = await inquireTransactions({ sortCode: '123456', accountNumber: '00000001' })

    expect(result.type).toBe('backend-error')
    if (result.type === 'backend-error') {
      expect(result.status).toBe(400)
      expect(result.error.code).toBe('ERR-001')
    }
  })

  it('builds detail endpoint with exact five-part identity', async () => {
    const fetchSpy = vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(
        JSON.stringify({
          found: true,
          transaction: {
            transactionId: '123456-00000001-20260728-143015-000000000123',
            sortCode: '123456',
            accountNumber: '00000001',
            date: '20260728',
            time: '143015',
            reference: '000000000123',
            type: 'CRD',
            description: 'Payroll deposit',
            amount: 125.5,
          },
        }),
        {
          status: 200,
          headers: {
            'content-type': 'application/json',
            'X-Correlation-ID': 'corr-detail',
          },
        },
      ),
    )

    const result = await inquireTransactionDetail({
      sortCode: '123456',
      accountNumber: '00000001',
      date: '20260728',
      time: '143015',
      reference: '000000000123',
    })

    expect(fetchSpy).toHaveBeenCalledWith(
      '/api/v1/accounts/123456/00000001/transactions/20260728/143015/000000000123',
      expect.any(Object),
    )
    expect(result.type).toBe('success')
  })

  it('returns backend error shape for detail 500 responses', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(
        JSON.stringify({
          code: 'ERR-500',
          message: 'Service unavailable due to infrastructure failure',
          correlationId: 'corr-500',
        }),
        {
          status: 500,
          headers: {
            'content-type': 'application/json',
            'X-Correlation-ID': 'corr-500',
          },
        },
      ),
    )

    const result = await inquireTransactionDetail({
      sortCode: '123456',
      accountNumber: '00000001',
      date: '20260728',
      time: '143015',
      reference: '000000000123',
    })

    expect(result.type).toBe('backend-error')
    if (result.type === 'backend-error') {
      expect(result.status).toBe(500)
      expect(result.error.code).toBe('ERR-500')
      expect(result.error.correlationId).toBe('corr-500')
    }
  })
})
