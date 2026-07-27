export interface LegacyStatus {
  success: 'Y' | 'N'
  failCode: string
  customerFound: 'Y' | 'N'
}

export interface AccountSummary {
  eyecatcher: string
  customerNumber: string
  sortCode: string
  accountNumber: string
  accountType: string
  interestRate: number
  openedDate: string
  overdraftLimit: number
  lastStatementDate: string
  nextStatementDate: string
  availableBalance: number
  actualBalance: number
}

export interface CustomerAccountInquiryResponse {
  legacyStatus: LegacyStatus
  customerNumber: string
  numberOfAccounts: number
  accounts: AccountSummary[]
}

export interface CustomerAccountValidationDetail {
  field: string
  reason: string
}

export interface CustomerAccountErrorPayload {
  type: string
  message: string
  details?: CustomerAccountValidationDetail[]
}

export interface CustomerAccountError {
  error: CustomerAccountErrorPayload
}

export type CustomerAccountInquiryResult =
  | {
      type: 'success'
      status: 200
      data: CustomerAccountInquiryResponse
    }
  | {
      type: 'backend-error'
      status: 400 | 500
      error: CustomerAccountError
    }
  | {
      type: 'timeout'
      message: string
    }
  | {
      type: 'network-error'
      message: string
    }
