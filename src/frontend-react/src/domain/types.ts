export type LookupMode = 'SPECIFIC' | 'RANDOM' | 'LATEST'

export interface LegacyInquiryStatus {
  inquirySuccess: string
  inquiryFailCode: string
  message: string
}

export interface AddressResponse {
  line1: string | null
  line2: string | null
  city: string | null
  postcode: string | null
  country: string | null
}

export interface CustomerResponse {
  eyecatcher: string | null
  sortCode: string | null
  customerNumber: string | null
  title: string | null
  firstName: string | null
  lastName: string | null
  dateOfBirth: string | null
  phone: string | null
  address: AddressResponse | null
  status: string | null
  createdDate: string | null
  creditScore: number | null
  creditScoreReviewDate: string | null
}

export interface RiskAssessmentResponse {
  riskRating: string
  reviewRequired: boolean
  reasons: string[]
}

export interface CustomerInquiryResponse {
  legacyStatus: LegacyInquiryStatus
  lookupMode: LookupMode
  customer: CustomerResponse | null
  riskAssessment: RiskAssessmentResponse | null
}

export interface FieldErrorResponse {
  field: string
  message: string
}

export interface ErrorResponse {
  errorCode: string
  message: string
  fieldErrors?: FieldErrorResponse[]
}
