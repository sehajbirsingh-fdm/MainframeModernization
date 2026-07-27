import { afterEach, describe, expect, it, vi } from 'vitest'
import { inquireCustomerAccounts } from './customerAccountInquiryClient'

describe('inquireCustomerAccounts', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('returns success payload for 200 response', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(
        JSON.stringify({
          legacyStatus: { success: 'Y', failCode: '0', customerFound: 'Y' },
          customerNumber: '0000000001',
          numberOfAccounts: 0,
          accounts: [],
        }),
        { status: 200, headers: { 'content-type': 'application/json' } },
      ),
    )

    const result = await inquireCustomerAccounts('0000000001')

    expect(result.type).toBe('success')
    if (result.type === 'success') {
      expect(result.data.legacyStatus.success).toBe('Y')
    }
  })

  it('returns backend error for 400/500 responses', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(JSON.stringify({ error: { type: 'VALIDATION_ERROR', message: 'Validation failed', details: [] } }), {
        status: 400,
        headers: { 'content-type': 'application/json' },
      }),
    )

    const result = await inquireCustomerAccounts('123')

    expect(result.type).toBe('backend-error')
    if (result.type === 'backend-error') {
      expect(result.status).toBe(400)
      expect(result.error.error.type).toBe('VALIDATION_ERROR')
    }
  })

  it('returns timeout on aborted requests', async () => {
    vi.spyOn(globalThis, 'fetch').mockRejectedValue(new DOMException('aborted', 'AbortError'))

    const result = await inquireCustomerAccounts('0000000001')

    expect(result.type).toBe('timeout')
  })

  it('returns network error on transport failure', async () => {
    vi.spyOn(globalThis, 'fetch').mockRejectedValue(new Error('network down'))

    const result = await inquireCustomerAccounts('0000000001')

    expect(result.type).toBe('network-error')
  })
})
