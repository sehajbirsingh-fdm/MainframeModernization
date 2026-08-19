package com.bankofz.mainframemodernization.inqstmt.controller;

import com.bankofz.mainframemodernization.inqstmt.domain.ErrorResponse;
import com.bankofz.mainframemodernization.inqstmt.exception.StatementNotFoundException;
import com.bankofz.mainframemodernization.inqstmt.exception.StatementTechnicalException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice(basePackageClasses = AccountStatementController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AccountStatementExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountStatementExceptionHandler.class);

    @ExceptionHandler({
            ConstraintViolationException.class,
            MethodArgumentTypeMismatchException.class,
            MethodArgumentNotValidException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ErrorResponse> handleValidation(Exception exception) {
        LOGGER.warn("event=inqstmt_error status=400 code=ERR-001");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(error("ERR-001", "Validation failed"));
    }

    @ExceptionHandler(StatementNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(StatementNotFoundException exception) {
        LOGGER.info("event=inqstmt_error status=404 code=ERR-404");
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(error("ERR-404", exception.getMessage()));
    }

    @ExceptionHandler(StatementTechnicalException.class)
    public ResponseEntity<ErrorResponse> handleTechnicalFailure(StatementTechnicalException exception) {
        LOGGER.warn("event=inqstmt_error status=500 code=ERR-500", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error("ERR-500", "Service unavailable due to infrastructure failure"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception exception) {
        LOGGER.error("event=inqstmt_error status=500 code=ERR-500", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error("ERR-500", "Service unavailable due to infrastructure failure"));
    }

    private ErrorResponse error(String code, String message) {
        return new ErrorResponse(code, message, MDC.get("correlationId"));
    }
}
