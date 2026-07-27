import { describe, expect, it } from 'vitest'
import { validateAccountInquiryInput } from './validation'

describe('account inquiry validation', () => {
  it('accepts valid sortcode and account number', () => {
    expect(validateAccountInquiryInput('123456', '12345678')).toEqual({})
  })

  it('rejects invalid sortcode and account number', () => {
    expect(validateAccountInquiryInput('12', 'ABC')).toEqual({
      sortcode: 'Sortcode must be exactly 6 digits.',
      accountNumber: 'Account number must be exactly 8 digits.',
    })
  })
})
