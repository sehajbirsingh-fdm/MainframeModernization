export interface LegacyStatus {
  success: 'Y' | 'N'
  failCode: string
  customerFound: 'Y' | 'N'
}

export interface CustomerSummary {
  customerNumber: string
  customerName: string
  sortCode: string
  customerType: string
}

export interface AccountSummary {
  accountNumber: string
  sortCode: string
  accountType: string
  accountTypeDescription: string
  availableBalance: number
  availableBalanceCurrency: string
  actualBalance: number
  actualBalanceCurrency: string
  interestRate: number
  overdraftLimit: number
  lastStatementDate: string | null
  nextStatementDate: string | null
}

export interface AccountsList {
  count: number
  accounts: AccountSummary[]
}

export interface CustomerAccountInquiryResponse {
  legacyStatus: LegacyStatus
  customer: CustomerSummary | null
  accounts: AccountsList | null
}

export interface CustomerAccountError {
  code: string
  message: string
  details?: Array<{ field: string; message: string }>
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
