package com.bankofz.mainframemodernization.inqtran.mapper;

import com.bankofz.mainframemodernization.inqtran.domain.TransactionRecord;
import com.bankofz.mainframemodernization.inqtran.repository.model.TransactionRow;
import org.springframework.stereotype.Component;

@Component
public class TransactionInquiryMapper {

    public TransactionRecord toRecord(TransactionRow row) {
        return new TransactionRecord(
                composeTransactionId(row),
                row.sortCode(),
                row.accountNumber(),
                row.date(),
                row.time(),
                row.reference(),
                row.type(),
                row.description(),
                row.amount()
        );
    }

    private String composeTransactionId(TransactionRow row) {
        return String.join(
                "-",
                row.sortCode(),
                row.accountNumber(),
                row.date(),
                row.time(),
                row.reference()
        );
    }
}
