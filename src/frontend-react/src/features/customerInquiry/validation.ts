export const SORT_CODE_REGEX = /^[0-9]{6}$/
export const CUSTOMER_NUMBER_REGEX = /^[0-9]{10}$/

export interface ValidationErrors {
  sortCode?: string
  customerNumber?: string
}

export function validateInquiryInput(sortCode: string, customerNumber: string): ValidationErrors {
  const errors: ValidationErrors = {}

  if (!SORT_CODE_REGEX.test(sortCode)) {
    errors.sortCode = 'Sort code must be exactly 6 digits.'
  }

  if (!CUSTOMER_NUMBER_REGEX.test(customerNumber)) {
    errors.customerNumber = 'Customer number must be exactly 10 digits.'
  }

  return errors
}
