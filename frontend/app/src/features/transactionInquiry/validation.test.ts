import { describe, expect, it } from 'vitest'
import { validateTransactionInquiryInput } from './validation'

describe('validateTransactionInquiryInput', () => {
  it('returns no errors for valid inputs', () => {
    expect(validateTransactionInquiryInput('123456', '00000001', '20260701', '20260731', '50', '0')).toEqual({})
  })

  it('returns errors for invalid mandatory and optional inputs', () => {
    expect(validateTransactionInquiryInput('12', '1', '202607', 'abc', '-1', '-2')).toEqual({
      sortCode: 'Sort code must be exactly 6 digits.',
      accountNumber: 'Account number must be exactly 8 digits.',
      fromDate: 'From date must be 8 digits in YYYYMMDD format.',
      toDate: 'To date must be 8 digits in YYYYMMDD format.',
      limit: 'Limit must be a non-negative integer.',
      offset: 'Offset must be a non-negative integer.',
    })
  })
})
