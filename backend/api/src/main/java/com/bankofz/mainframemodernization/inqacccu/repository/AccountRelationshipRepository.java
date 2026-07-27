package com.bankofz.mainframemodernization.inqacccu.repository;

import com.bankofz.mainframemodernization.inqacccu.repository.model.RelationshipProjection;

import java.util.Optional;

public interface AccountRelationshipRepository {

    Optional<RelationshipProjection> findByCustomerNumber(String customerNumber);
}
