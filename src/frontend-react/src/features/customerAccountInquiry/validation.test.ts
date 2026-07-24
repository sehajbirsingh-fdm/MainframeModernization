import { describe, expect, it } from 'vitest'
import { validateCustomerAccountInput } from './validation'

describe('validateCustomerAccountInput', () => {
  it('accepts 10-digit customer numbers including reserved values', () => {
    expect(validateCustomerAccountInput('1234567890')).toEqual({})
    expect(validateCustomerAccountInput('0000000000')).toEqual({})
    expect(validateCustomerAccountInput('9999999999')).toEqual({})
  })

  it('rejects invalid values', () => {
    expect(validateCustomerAccountInput('ABC')).toEqual({
      customerNumber: 'Customer number must be exactly 10 digits.',
    })
  })
})
