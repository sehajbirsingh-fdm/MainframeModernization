export interface UpdateCustomerAddress {
  addressLine1: string
  addressLine2: string
  city: string
  postalCode: string
  country: string
}

export interface UpdateCustomerRequest {
  title: string
  firstName: string
  lastName: string
  dateOfBirth: string
  phoneNumber: string
  address: UpdateCustomerAddress
  customerStatus: string
}

export interface LegacyUpdateStatus {
  updSuccess: string
  updFailCode: string
}

export interface UpdateCustomerResponse {
  customerNumber: string
  sortCode: string
  title: string
  firstName: string
  lastName: string
  dateOfBirth: string | null
  phoneNumber: string
  address: UpdateCustomerAddress
  customerStatus: string
  createdDate: string | null
  creditScore: number | null
  creditScoreReviewDate: string | null
  legacyStatus: LegacyUpdateStatus
}

export interface UpdateCustomerErrorEnvelope {
  error: {
    code: string
    message: string
    legacyFailCode?: string
    correlationId: string
    timestamp: string
  }
}

export type UpdateCustomerResult =
  | {
      type: 'success'
      status: 200
      data: UpdateCustomerResponse
      correlationId: string
    }
  | {
      type: 'backend-error'
      status: 400 | 404 | 422 | 500
      error: UpdateCustomerErrorEnvelope
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
