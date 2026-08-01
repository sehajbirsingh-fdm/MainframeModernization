export const SORT_CODE_REGEX = /^\d{6}$/
export const ACCOUNT_NUMBER_REGEX = /^\d{8}$/
export const DATE_REGEX = /^\d{8}$/

export interface TransactionInquiryValidationErrors {
  sortCode?: string
  accountNumber?: string
  fromDate?: string
  toDate?: string
  limit?: string
  offset?: string
}

export function validateTransactionInquiryInput(
  sortCode: string,
  accountNumber: string,
  fromDate: string,
  toDate: string,
  limit: string,
  offset: string,
): TransactionInquiryValidationErrors {
  const errors: TransactionInquiryValidationErrors = {}

  if (!SORT_CODE_REGEX.test(sortCode)) {
    errors.sortCode = 'Sort code must be exactly 6 digits.'
  }

  if (!ACCOUNT_NUMBER_REGEX.test(accountNumber)) {
    errors.accountNumber = 'Account number must be exactly 8 digits.'
  }

  if (fromDate && !DATE_REGEX.test(fromDate)) {
    errors.fromDate = 'From date must be 8 digits in YYYYMMDD format.'
  }

  if (toDate && !DATE_REGEX.test(toDate)) {
    errors.toDate = 'To date must be 8 digits in YYYYMMDD format.'
  }

  if (limit && (!/^\d+$/.test(limit) || Number(limit) < 0)) {
    errors.limit = 'Limit must be a non-negative integer.'
  }

  if (offset && (!/^\d+$/.test(offset) || Number(offset) < 0)) {
    errors.offset = 'Offset must be a non-negative integer.'
  }

  return errors
}
