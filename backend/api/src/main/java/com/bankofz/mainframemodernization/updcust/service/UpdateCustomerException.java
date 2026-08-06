package com.bankofz.mainframemodernization.updcust.service;

import org.springframework.http.HttpStatus;

public class UpdateCustomerException extends RuntimeException {

    private final String errorCode;
    private final String legacyFailCode;
    private final HttpStatus httpStatus;

    public UpdateCustomerException(String message, String errorCode, String legacyFailCode, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.legacyFailCode = legacyFailCode;
        this.httpStatus = httpStatus;
    }

    public String errorCode() {
        return errorCode;
    }

    public String legacyFailCode() {
        return legacyFailCode;
    }

    public HttpStatus httpStatus() {
        return httpStatus;
    }
}
