export const SORT_CODE_REGEX = /^\d{6}$/
export const ACCOUNT_NUMBER_REGEX = /^\d{8}$/
export const PERIOD_REGEX = /^\d{4}(0[1-9]|1[0-2])$/

export interface StatementInquiryValidationErrors {
  sortCode?: string
  accountNumber?: string
  period?: string
}

export function validateStatementInquiryInput(
  sortCode: string,
  accountNumber: string,
  period: string,
): StatementInquiryValidationErrors {
  const errors: StatementInquiryValidationErrors = {}

  if (!SORT_CODE_REGEX.test(sortCode)) {
    errors.sortCode = 'Sort code must be exactly 6 digits.'
  }

  if (!ACCOUNT_NUMBER_REGEX.test(accountNumber)) {
    errors.accountNumber = 'Account number must be exactly 8 digits.'
  }

  if (!PERIOD_REGEX.test(period)) {
    errors.period = 'Period must be YYYYMM with month between 01 and 12.'
  }

  return errors
}
