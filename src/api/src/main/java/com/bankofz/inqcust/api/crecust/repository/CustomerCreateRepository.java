package com.bankofz.inqcust.api.crecust.repository;

import com.bankofz.inqcust.api.domain.CustomerRecord;

public interface CustomerCreateRepository {

    long nextCustomerNumber(String sortCode);

    CustomerRecord save(CustomerRecord customerRecord);
}
