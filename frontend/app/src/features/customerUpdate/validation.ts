export interface CustomerUpdateValidationErrors {
  title?: string
  firstName?: string
  lastName?: string
  dateOfBirth?: string
  phoneNumber?: string
  addressLine1?: string
  addressLine2?: string
  city?: string
  postalCode?: string
  country?: string
  customerStatus?: string
  payload?: string
}

const ALLOWED_TITLES = new Set(['Professor', 'Mr', 'Mrs', 'Miss', 'Ms', 'Dr', 'Drs', 'Lord', 'Sir', 'Lady', ''])

function startsWithSpaceOrBlank(value: string): boolean {
  return value.length === 0 || /^\s*$/.test(value) || value[0] === ' '
}

export function validateCustomerUpdateInput(input: {
  title: string
  firstName: string
  lastName: string
  dateOfBirth: string
  phoneNumber: string
  addressLine1: string
  addressLine2: string
  city: string
  postalCode: string
  country: string
  customerStatus: string
}): CustomerUpdateValidationErrors {
  const errors: CustomerUpdateValidationErrors = {}

  if (!ALLOWED_TITLES.has(input.title.trimEnd())) {
    errors.title = 'Title is invalid based on legacy allow-list.'
  }

  if (input.title.length > 10) {
    errors.title = 'Title must be 10 characters or fewer.'
  }

  if (input.firstName.length > 50) {
    errors.firstName = 'First name must be 50 characters or fewer.'
  }

  if (input.lastName.length > 50) {
    errors.lastName = 'Last name must be 50 characters or fewer.'
  }

  if (input.phoneNumber.length > 20) {
    errors.phoneNumber = 'Phone number must be 20 characters or fewer.'
  }

  if (input.addressLine1.length > 50) {
    errors.addressLine1 = 'Address Line 1 must be 50 characters or fewer.'
  }

  if (input.addressLine2.length > 50) {
    errors.addressLine2 = 'Address Line 2 must be 50 characters or fewer.'
  }

  if (input.city.length > 50) {
    errors.city = 'City must be 50 characters or fewer.'
  }

  if (input.postalCode.length > 10) {
    errors.postalCode = 'Postal code must be 10 characters or fewer.'
  }

  if (input.country.length > 50) {
    errors.country = 'Country must be 50 characters or fewer.'
  }

  if (input.customerStatus.length > 10) {
    errors.customerStatus = 'Customer status must be 10 characters or fewer.'
  }

  if (input.dateOfBirth && !/^\d{4}-\d{2}-\d{2}$/.test(input.dateOfBirth)) {
    errors.dateOfBirth = 'Date of birth must match yyyy-MM-dd.'
  }

  if (
    startsWithSpaceOrBlank(input.firstName) &&
    startsWithSpaceOrBlank(input.lastName) &&
    startsWithSpaceOrBlank(input.addressLine1)
  ) {
    errors.payload = 'At least one meaningful update is required in first name, last name, or address line 1.'
  }

  return errors
}
