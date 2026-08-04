import { afterEach, describe, expect, it, vi } from 'vitest'
import { updateCustomer } from './customerUpdateClient'

describe('updateCustomer', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('normalizes success invariants for fail code and creditScoreReviewDate', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(
        JSON.stringify({
          customerNumber: '0000000001',
          sortCode: '123456',
          title: 'Ms',
          firstName: 'Jane',
          lastName: 'Doe',
          dateOfBirth: '1990-01-01',
          phoneNumber: '4165550111',
          address: {
            addressLine1: '10 Bay Street',
            addressLine2: 'Suite 200',
            city: 'Toronto',
            postalCode: 'M5J2N8',
            country: 'Canada',
          },
          customerStatus: 'ACTIVE',
          createdDate: '2010-06-15',
          creditScore: 742,
          creditScoreReviewDate: 'invalid-date',
          legacyStatus: {
            updSuccess: 'Y',
            updFailCode: '3',
          },
        }),
        { status: 200, headers: { 'content-type': 'application/json' } },
      ),
    )

    const result = await updateCustomer('0000000001', '123456', {
      title: 'Ms',
      firstName: 'Jane',
      lastName: 'Doe',
      dateOfBirth: '1990-01-01',
      phoneNumber: '4165550111',
      address: {
        addressLine1: '10 Bay Street',
        addressLine2: 'Suite 200',
        city: 'Toronto',
        postalCode: 'M5J2N8',
        country: 'Canada',
      },
      customerStatus: 'ACTIVE',
    })

    expect(result.type).toBe('success')
    if (result.type === 'success') {
      expect(result.data.legacyStatus.updFailCode).toBe(' ')
      expect(result.data.creditScoreReviewDate).toBeNull()
    }
  })

  it('returns backend-error for 401', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(
        JSON.stringify({
          error: {
            code: 'ERR-002',
            message: 'Unauthorized',
            correlationId: 'c1',
            timestamp: '2026-08-04T10:00:00Z',
          },
        }),
        { status: 401, headers: { 'content-type': 'application/json' } },
      ),
    )

    const result = await updateCustomer('0000000001', '123456', {
      title: 'Ms',
      firstName: 'Jane',
      lastName: 'Doe',
      dateOfBirth: '1990-01-01',
      phoneNumber: '4165550111',
      address: {
        addressLine1: '10 Bay Street',
        addressLine2: 'Suite 200',
        city: 'Toronto',
        postalCode: 'M5J2N8',
        country: 'Canada',
      },
      customerStatus: 'ACTIVE',
    })

    expect(result.type).toBe('backend-error')
    if (result.type === 'backend-error') {
      expect(result.status).toBe(401)
      expect(result.error.error.code).toBe('ERR-002')
    }
  })

  it('returns backend-error for 403', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(
        JSON.stringify({
          error: {
            code: 'ERR-003',
            message: 'Forbidden',
            correlationId: 'c2',
            timestamp: '2026-08-04T10:00:00Z',
          },
        }),
        { status: 403, headers: { 'content-type': 'application/json' } },
      ),
    )

    const result = await updateCustomer('0000000001', '123456', {
      title: 'Ms',
      firstName: 'Jane',
      lastName: 'Doe',
      dateOfBirth: '1990-01-01',
      phoneNumber: '4165550111',
      address: {
        addressLine1: '10 Bay Street',
        addressLine2: 'Suite 200',
        city: 'Toronto',
        postalCode: 'M5J2N8',
        country: 'Canada',
      },
      customerStatus: 'ACTIVE',
    })

    expect(result.type).toBe('backend-error')
    if (result.type === 'backend-error') {
      expect(result.status).toBe(403)
      expect(result.error.error.code).toBe('ERR-003')
    }
  })
})
