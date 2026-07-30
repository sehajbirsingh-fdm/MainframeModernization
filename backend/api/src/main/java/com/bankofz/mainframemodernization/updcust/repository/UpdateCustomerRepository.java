package com.bankofz.mainframemodernization.updcust.repository;

import com.bankofz.mainframemodernization.inqcust.domain.CustomerRecord;

public interface UpdateCustomerRepository {

    CustomerRecord update(CustomerRecord customerRecord);
}
