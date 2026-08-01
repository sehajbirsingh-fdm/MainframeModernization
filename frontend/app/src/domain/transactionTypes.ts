export interface Transaction {
  transactionId: string
  sortCode: string
  accountNumber: string
  date: string
  time: string
  reference: string
  type: string
  description: string
  amount: number
}

export interface TransactionInquiryResponse {
  sortCode: string
  accountNumber: string
  fromDate: string | null
  toDate: string | null
  limit: number
  offset: number
  totalCount: number
  returnedCount: number
  transactions: Transaction[]
}

export interface TransactionErrorResponse {
  code: string
  message: string
  correlationId?: string | null
}

export interface TransactionInquiryRequest {
  sortCode: string
  accountNumber: string
  fromDate?: string
  toDate?: string
  limit?: number
  offset?: number
}

export type TransactionInquiryResult =
  | {
      type: 'success'
      status: 200
      data: TransactionInquiryResponse
      correlationId: string
    }
  | {
      type: 'backend-error'
      status: 400 | 500
      error: TransactionErrorResponse
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
