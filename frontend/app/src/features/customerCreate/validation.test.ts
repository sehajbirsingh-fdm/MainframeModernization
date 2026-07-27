import { describe, expect, it } from 'vitest'
import { validateCustomerCreateInput } from './validation'

describe('validateCustomerCreateInput', () => {
  it('returns no errors for valid data', () => {
    const errors = validateCustomerCreateInput({
      title: 'Mr',
      firstName: 'John',
      lastName: 'Smith',
      dobDay: '01',
      dobMonth: '01',
      dobYear: '1990',
      createdDay: '22',
      createdMonth: '07',
      createdYear: '2026',
      phone: '4165550101',
      addressLine1: '1 Main',
      city: 'Toronto',
      postcode: 'M5H2N2',
      country: 'Canada',
      status: 'ACTIVE',
    })

    expect(Object.keys(errors)).toHaveLength(0)
  })

  it('returns title and dob errors for invalid values', () => {
    const errors = validateCustomerCreateInput({
      title: 'Captain',
      firstName: 'John',
      lastName: 'Smith',
      dobDay: '31',
      dobMonth: '02',
      dobYear: '1599',
      createdDay: '22',
      createdMonth: '07',
      createdYear: '2026',
      phone: '4165550101',
      addressLine1: '1 Main',
      city: 'Toronto',
      postcode: 'M5H2N2',
      country: 'Canada',
      status: 'ACTIVE',
    })

    expect(errors.title).toBeDefined()
    expect(errors.dateOfBirth).toBeDefined()
  })
})
