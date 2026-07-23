package com.bankofz.inqcust.api.inqacccu.repository;

import com.bankofz.inqcust.api.inqacccu.repository.model.RelationshipProjection;

import java.util.Optional;

public interface AccountRelationshipRepository {

    Optional<RelationshipProjection> findByCustomerNumber(String customerNumber);
}
