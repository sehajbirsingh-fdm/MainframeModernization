import { describe, expect, it } from 'vitest'
import { validateStatementInquiryInput } from './validation'

describe('validateStatementInquiryInput', () => {
  it('accepts valid values', () => {
    expect(validateStatementInquiryInput('123456', '00000001', '202607')).toEqual({})
  })

  it('rejects malformed values', () => {
    expect(validateStatementInquiryInput('12345', '123', '202613')).toEqual({
      sortCode: 'Sort code must be exactly 6 digits.',
      accountNumber: 'Account number must be exactly 8 digits.',
      period: 'Period must be YYYYMM with month between 01 and 12.',
    })
  })
})
