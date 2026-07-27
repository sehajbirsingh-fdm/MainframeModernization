export const TITLE_ALLOWED = new Set(['Professor', 'Mr', 'Mrs', 'Miss', 'Ms', 'Dr', 'Drs', 'Lord', 'Sir', 'Lady', ''])

export interface CustomerCreateValidationErrors {
  title?: string
  firstName?: string
  lastName?: string
  dateOfBirth?: string
  createdDate?: string
  phone?: string
  addressLine1?: string
  city?: string
  postcode?: string
  country?: string
  status?: string
}

function isValidDate(day: number, month: number, year: number): boolean {
  const date = new Date(Date.UTC(year, month - 1, day))
  return (
    date.getUTCFullYear() === year &&
    date.getUTCMonth() === month - 1 &&
    date.getUTCDate() === day
  )
}

export function validateCustomerCreateInput(input: {
  title: string
  firstName: string
  lastName: string
  dobDay: string
  dobMonth: string
  dobYear: string
  createdDay: string
  createdMonth: string
  createdYear: string
  phone: string
  addressLine1: string
  city: string
  postcode: string
  country: string
  status: string
}): CustomerCreateValidationErrors {
  const errors: CustomerCreateValidationErrors = {}

  if (!TITLE_ALLOWED.has(input.title.trim())) {
    errors.title = 'Title is invalid based on legacy allow-list.'
  }

  if (!input.firstName.trim()) {
    errors.firstName = 'First name is required.'
  }

  if (!input.lastName.trim()) {
    errors.lastName = 'Last name is required.'
  }

  if (!input.phone.trim()) {
    errors.phone = 'Phone is required.'
  }

  if (!input.addressLine1.trim()) {
    errors.addressLine1 = 'Address line 1 is required.'
  }

  if (!input.city.trim()) {
    errors.city = 'City is required.'
  }

  if (!input.postcode.trim()) {
    errors.postcode = 'Postcode is required.'
  }

  if (!input.country.trim()) {
    errors.country = 'Country is required.'
  }

  if (!input.status.trim()) {
    errors.status = 'Status is required.'
  }

  const dobDay = Number(input.dobDay)
  const dobMonth = Number(input.dobMonth)
  const dobYear = Number(input.dobYear)
  if (!Number.isInteger(dobDay) || !Number.isInteger(dobMonth) || !Number.isInteger(dobYear)) {
    errors.dateOfBirth = 'Date of birth is required.'
  } else if (dobYear < 1601) {
    errors.dateOfBirth = 'Date of birth year must be 1601 or later.'
  } else if (!isValidDate(dobDay, dobMonth, dobYear)) {
    errors.dateOfBirth = 'Date of birth is invalid.'
  }

  const createdDay = Number(input.createdDay)
  const createdMonth = Number(input.createdMonth)
  const createdYear = Number(input.createdYear)
  if (!Number.isInteger(createdDay) || !Number.isInteger(createdMonth) || !Number.isInteger(createdYear)) {
    errors.createdDate = 'Created date is required.'
  } else if (!isValidDate(createdDay, createdMonth, createdYear)) {
    errors.createdDate = 'Created date is invalid.'
  }

  return errors
}
