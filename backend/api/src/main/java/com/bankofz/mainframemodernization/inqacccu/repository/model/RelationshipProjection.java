package com.bankofz.mainframemodernization.inqacccu.repository.model;

import java.util.List;

public record RelationshipProjection(
        CustomerProjection customer,
        List<AccountProjection> accounts
) {
}
