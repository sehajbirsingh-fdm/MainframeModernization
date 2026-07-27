package com.bankofz.mainframemodernization.crecust.repository;

import com.bankofz.mainframemodernization.inqcust.domain.CustomerRecord;

public interface CustomerCreateRepository {

    long nextCustomerNumber(String sortCode);

    CustomerRecord save(CustomerRecord customerRecord);
}
