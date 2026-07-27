package com.bankofz.mainframemodernization.crecust.service;

import org.springframework.http.HttpStatus;

public class CustomerCreateException extends RuntimeException {

    private final String errorCode;
    private final String legacyFailCode;
    private final HttpStatus httpStatus;

    public CustomerCreateException(String message, String errorCode, String legacyFailCode, HttpStatus httpStatus) {
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
