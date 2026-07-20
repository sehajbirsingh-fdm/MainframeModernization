package com.bankofz.inqcust.api.inqacc.controller;

import com.bankofz.inqcust.api.inqacc.domain.ErrorResponse;
import com.bankofz.inqcust.api.inqacc.exception.AccountNotFoundException;
import com.bankofz.inqcust.api.inqacc.exception.RepositoryUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice(basePackageClasses = AccountInquiryController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AccountInquiryExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountInquiryExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleValidation(IllegalArgumentException exception) {
        LOGGER.warn("event=inqacc_inquiry_error status=400 code=ERR-001");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(error("ERR-001", exception.getMessage(), null));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(BadCredentialsException exception) {
        LOGGER.warn("event=inqacc_inquiry_error status=401 code=ERR-002");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(error("ERR-002", "Unauthorized", null));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(AccessDeniedException exception) {
        LOGGER.warn("event=inqacc_inquiry_error status=403 code=ERR-003");
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(error("ERR-003", "Forbidden", null));
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(AccountNotFoundException exception) {
        LOGGER.info("event=inqacc_inquiry_error status=404 code=ERR-004");
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(error("ERR-004", exception.getMessage(), null));
    }

    @ExceptionHandler(RepositoryUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleRepositoryUnavailable(RepositoryUnavailableException exception) {
        LOGGER.warn("event=inqacc_inquiry_error status=503 code=ERR-005");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(error("ERR-005", "Repository unavailable", null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception exception) {
        LOGGER.error("event=inqacc_inquiry_error status=500 code=ERR-006", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error("ERR-006", "Unexpected internal failure", null));
    }

    private ErrorResponse error(String code, String message, String details) {
        String correlationId = MDC.get("correlationId");
        return new ErrorResponse(new ErrorResponse.ErrorBody(
                code,
                message,
                Instant.now().toString(),
                correlationId,
                details
        ));
    }
}
