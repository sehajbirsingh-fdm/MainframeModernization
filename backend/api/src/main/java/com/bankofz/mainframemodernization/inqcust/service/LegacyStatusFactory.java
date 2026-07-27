package com.bankofz.mainframemodernization.inqcust.service;

import com.bankofz.mainframemodernization.inqcust.domain.LegacyInquiryStatus;
import org.springframework.stereotype.Component;

@Component
public class LegacyStatusFactory {

    public LegacyInquiryStatus success() {
        return new LegacyInquiryStatus("Y", "0", "Inquiry successful");
    }

    public LegacyInquiryStatus specificNotFound() {
        return new LegacyInquiryStatus("N", "1", "Customer not found");
    }

    public LegacyInquiryStatus randomNotFound() {
        return new LegacyInquiryStatus("N", "1", "Random lookup failed");
    }

    public LegacyInquiryStatus latestNotFound() {
        return new LegacyInquiryStatus("N", "9", "Latest customer not found");
    }
}
