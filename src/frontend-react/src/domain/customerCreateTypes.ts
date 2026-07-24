export interface CreateCustomerDateParts {
  day: number
  month: number
  year: number
}

export interface CreateCustomerAddress {
  line1: string
  line2: string
  city: string
  postcode: string
  country: string
}

export interface CreateCustomerRequest {
  title: string
  firstName: string
  lastName: string
  dateOfBirth: CreateCustomerDateParts
  createdDate: CreateCustomerDateParts
  phone: string
  address: CreateCustomerAddress
  status: string
}

export interface LegacyCreateStatus {
  commSuccess: string
  commFailCode: string
}

export interface CreateCustomerResponse {
  eyecatcher: string
  sortCode: string
  customerNumber: string
  title: string
  firstName: string
  lastName: string
  dateOfBirth: string
  phone: string
  addressLine1: string
  addressLine2: string
  city: string
  postcode: string
  country: string
  status: string
  createdDate: string
  creditScore: number
  creditScoreReviewDate: string
  legacyStatus: LegacyCreateStatus
}

export interface CreateCustomerErrorEnvelope {
  error: {
    code: string
    message: string
    legacyFailCode?: string
    correlationId: string
    timestamp: string
  }
}

export type CreateCustomerResult =
  | {
      type: 'success'
      status: 201
      data: CreateCustomerResponse
      correlationId: string
    }
  | {
      type: 'backend-error'
      status: 400 | 422 | 500 | 503
      error: CreateCustomerErrorEnvelope
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
