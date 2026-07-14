import { describe, expect, it } from 'vitest'
import { validateInquiryInput } from './validation'

describe('validateInquiryInput', () => {
  it('returns empty errors for valid 6 and 10 digit values', () => {
    expect(validateInquiryInput('123456', '1234567890')).toEqual({})
  })

  it('rejects invalid sort code', () => {
    expect(validateInquiryInput('12345', '1234567890')).toEqual({
      sortCode: 'Sort code must be exactly 6 digits.',
    })
  })

  it('rejects invalid customer number', () => {
    expect(validateInquiryInput('123456', '123456789')).toEqual({
      customerNumber: 'Customer number must be exactly 10 digits.',
    })
  })
})
