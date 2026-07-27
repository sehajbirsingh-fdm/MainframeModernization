package com.bankofz.mainframemodernization.inqacccu.exception;

public class RetrievalStageFailureException extends RuntimeException {

    private final String customerNumber;
    private final String failCode;

    private RetrievalStageFailureException(String customerNumber, String failCode, String message) {
        super(message);
        this.customerNumber = customerNumber;
        this.failCode = failCode;
    }

    public static RetrievalStageFailureException openStage(String customerNumber) {
        return new RetrievalStageFailureException(customerNumber, "2", "Retrieval open-stage failure");
    }

    public static RetrievalStageFailureException fetchStage(String customerNumber) {
        return new RetrievalStageFailureException(customerNumber, "3", "Retrieval fetch-stage failure");
    }

    public static RetrievalStageFailureException closeStage(String customerNumber) {
        return new RetrievalStageFailureException(customerNumber, "4", "Retrieval close-stage failure");
    }

    public String customerNumber() {
        return customerNumber;
    }

    public String failCode() {
        return failCode;
    }
}