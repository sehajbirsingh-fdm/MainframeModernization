package com.bankofz.mainframemodernization.inqacccu.service;

import com.bankofz.mainframemodernization.inqacccu.domain.AccountRelationshipResponse;
import com.bankofz.mainframemodernization.inqacccu.exception.RetrievalStageFailureException;
import com.bankofz.mainframemodernization.inqacccu.repository.AccountRelationshipRepository;
import org.springframework.stereotype.Service;

@Service
public class AccountRelationshipService {

    private final AccountRelationshipRepository repository;
    private final AccountRelationshipMapper mapper;

    public AccountRelationshipService(
            AccountRelationshipRepository repository,
            AccountRelationshipMapper mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public AccountRelationshipResponse inquire(String customerNumber) {
        try {
            return repository.findByCustomerNumber(customerNumber)
                    .map(mapper::toSuccessResponse)
                    .orElseGet(() -> mapper.toNotFoundResponse(customerNumber));
        } catch (RetrievalStageFailureException exception) {
            return mapper.toRetrievalFailureResponse(exception.customerNumber(), exception.failCode());
        }
    }
}
