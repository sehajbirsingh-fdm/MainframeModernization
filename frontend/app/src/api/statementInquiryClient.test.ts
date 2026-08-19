import { afterEach, describe, expect, it, vi } from 'vitest'
import { inquireStatement } from './statementInquiryClient'

describe('inquireStatement', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('builds endpoint and sends bearer token header', async () => {
    const fetchSpy = vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(
        JSON.stringify({
          sortCode: '123456',
          accountNumber: '00000001',
          period: '202607',
          summary: {
            periodFrom: '20260701',
            periodTo: '20260731',
            openingBalance: 500,
            totalCredits: 100,
            totalDebits: 10,
            closingBalance: 590,
            transactionCount: 1,
          },
          entries: [],
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

    const result = await inquireStatement({
      sortCode: '123456',
      accountNumber: '00000001',
      period: '202607',
    })

    expect(fetchSpy).toHaveBeenCalledWith(
      '/api/v1/accounts/123456/00000001/statements/202607',
      expect.objectContaining({
        headers: expect.objectContaining({
          Authorization: 'Bearer valid-inqacc-inquirer-token',
        }),
      }),
    )
    expect(result.type).toBe('success')
  })

  it('returns backend-error for authorization failure', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(
        JSON.stringify({
          code: 'ERR-003',
          message: 'Forbidden',
          correlationId: 'corr-403',
        }),
        {
          status: 403,
          headers: {
            'content-type': 'application/json',
            'X-Correlation-ID': 'corr-403',
          },
        },
      ),
    )

    const result = await inquireStatement({
      sortCode: '123456',
      accountNumber: '00000001',
      period: '202607',
    })

    expect(result.type).toBe('backend-error')
    if (result.type === 'backend-error') {
      expect(result.status).toBe(403)
      expect(result.error.code).toBe('ERR-003')
    }
  })
})
