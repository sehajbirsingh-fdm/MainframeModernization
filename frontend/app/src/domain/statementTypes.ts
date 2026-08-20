export interface StatementSummary {
  periodFrom: string
  periodTo: string
  openingBalance: number
  totalCredits: number
  totalDebits: number
  closingBalance: number
  transactionCount: number
}

export interface StatementEntry {
  date: string
  time: string
  reference: string
  type: string
  description: string
  amount: number
}

export interface StatementResponse {
  sortCode: string
  accountNumber: string
  period: string
  summary: StatementSummary
  entries: StatementEntry[]
}

export interface StatementErrorResponse {
  code: string
  message: string
  correlationId?: string | null
}

export interface StatementRequest {
  sortCode: string
  accountNumber: string
  period: string
}

export type StatementInquiryResult =
  | {
      type: 'success'
      status: 200
      data: StatementResponse
      correlationId: string
    }
  | {
      type: 'backend-error'
      status: 400 | 401 | 403 | 404 | 500
      error: StatementErrorResponse
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
