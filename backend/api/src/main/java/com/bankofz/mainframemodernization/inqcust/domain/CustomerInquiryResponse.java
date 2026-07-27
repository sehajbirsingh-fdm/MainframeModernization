package com.bankofz.mainframemodernization.inqcust.domain;

public record CustomerInquiryResponse(
        LegacyInquiryStatus legacyStatus,
        LookupMode lookupMode,
        CustomerResponse customer,
        RiskAssessmentResponse riskAssessment
) {
}
