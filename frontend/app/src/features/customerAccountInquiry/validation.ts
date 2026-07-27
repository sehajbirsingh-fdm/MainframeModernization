export const CUSTOMER_NUMBER_REGEX = /^[0-9]{10}$/

export interface CustomerAccountValidationErrors {
  customerNumber?: string
}

export function validateCustomerAccountInput(customerNumber: string): CustomerAccountValidationErrors {
  if (!CUSTOMER_NUMBER_REGEX.test(customerNumber)) {
    return { customerNumber: 'Customer number must be exactly 10 digits.' }
  }

  return {}
}
