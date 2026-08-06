package com.bankofz.mainframemodernization.updcust.controller;

import com.bankofz.mainframemodernization.updcust.domain.UpdateCustomerErrorResponse;
import com.bankofz.mainframemodernization.updcust.service.UpdateCustomerException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.UUID;

@RestControllerAdvice(assignableTypes = UpdateCustomerController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class UpdateCustomerExceptionHandler {

    @ExceptionHandler(UpdateCustomerException.class)
    public ResponseEntity<UpdateCustomerErrorResponse> handleUpdateCustomerException(UpdateCustomerException exception) {
        String correlationId = correlationId();
        return ResponseEntity.status(exception.httpStatus())
                .body(error(exception.errorCode(), exception.getMessage(), exception.legacyFailCode(), correlationId));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    public ResponseEntity<UpdateCustomerErrorResponse> handleValidationException(Exception exception) {
        String correlationId = correlationId();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(error("UPDCUST-400-VALIDATION", "Invalid request payload", " ", correlationId));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<UpdateCustomerErrorResponse> handleUnexpectedException(Exception exception) {
        String correlationId = correlationId();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error("UPDCUST-500-UNEXPECTED", "Unexpected internal failure", " ", correlationId));
    }

    private String correlationId() {
        String correlationId = MDC.get("correlationId");
        return (correlationId == null || correlationId.isBlank()) ? UUID.randomUUID().toString() : correlationId;
    }

    private UpdateCustomerErrorResponse error(String code, String message, String legacyFailCode, String correlationId) {
        return new UpdateCustomerErrorResponse(
                new UpdateCustomerErrorResponse.ErrorBody(
                        code,
                        message,
                        legacyFailCode,
                        correlationId,
                        Instant.now().toString()
                )
        );
    }
}
