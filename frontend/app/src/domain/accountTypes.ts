export interface AccountResponse {
  eyecatcher: string
  customerNumber: string
  sortcode: string
  accountNumber: string
  accountType: string
  interestRate: number
  accountOpened: string
  overdraftLimit: number
  lastStatementDate: string
  nextStatementDate: string
  availableBalance: number
  actualBalance: number
}

export interface AccountErrorEnvelope {
  error: {
    code: string
    message: string
    details?: string
    timestamp: string
    correlationId: string
  }
}

export interface AccountInquiryRequest {
  sortcode: string
  accountNumber: string
}

export type AccountInquiryResult =
  | {
      type: 'success'
      status: 200
      data: AccountResponse
      correlationId: string
    }
  | {
      type: 'not-found'
      status: 404
      error: AccountErrorEnvelope
      correlationId: string
    }
  | {
      type: 'backend-error'
      status: 400 | 401 | 403 | 500 | 503
      error: AccountErrorEnvelope
      correlationId: string
    }
  | {
      type: 'timeout'
      message: string
    }
  | {
      type: 'network-error'
      message: string
    }
