package com.bankofz.mainframemodernization.inqcust.domain;

import java.util.List;

public record RiskAssessmentResponse(
        RiskRating riskRating,
        boolean reviewRequired,
        List<String> reasons
) {
}
