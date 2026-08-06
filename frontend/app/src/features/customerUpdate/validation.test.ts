import { describe, expect, it } from 'vitest'
import { validateCustomerUpdateInput } from './validation'

describe('validateCustomerUpdateInput', () => {
  it('rejects invalid title and empty meaningful payload', () => {
    const errors = validateCustomerUpdateInput({
      title: 'Captain',
      firstName: '',
      lastName: '',
      dateOfBirth: '',
      phoneNumber: '',
      addressLine1: '',
      addressLine2: '',
      city: '',
      postalCode: '',
      country: '',
      customerStatus: '',
    })

    expect(errors.title).toBeTruthy()
    expect(errors.payload).toBeTruthy()
  })

  it('passes with meaningful first name update and allowed title', () => {
    const errors = validateCustomerUpdateInput({
      title: 'Mr',
      firstName: 'Jane',
      lastName: '',
      dateOfBirth: '1990-01-01',
      phoneNumber: '',
      addressLine1: '',
      addressLine2: '',
      city: '',
      postalCode: '',
      country: '',
      customerStatus: '',
    })

    expect(Object.keys(errors)).toHaveLength(0)
  })
})
