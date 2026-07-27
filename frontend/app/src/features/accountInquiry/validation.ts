export const SORTCODE_REGEX = /^\d{6}$/
export const ACCOUNT_NUMBER_REGEX = /^\d{8}$/

export interface AccountValidationErrors {
  sortcode?: string
  accountNumber?: string
}

export function validateAccountInquiryInput(sortcode: string, accountNumber: string): AccountValidationErrors {
  const errors: AccountValidationErrors = {}

  if (!SORTCODE_REGEX.test(sortcode)) {
    errors.sortcode = 'Sortcode must be exactly 6 digits.'
  }

  if (!ACCOUNT_NUMBER_REGEX.test(accountNumber)) {
    errors.accountNumber = 'Account number must be exactly 8 digits.'
  }

  return errors
}
