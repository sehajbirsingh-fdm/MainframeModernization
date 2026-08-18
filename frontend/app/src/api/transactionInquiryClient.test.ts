import { afterEach, describe, expect, it, vi } from 'vitest'
import { inquireTransactions } from './transactionInquiryClient'

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
})
