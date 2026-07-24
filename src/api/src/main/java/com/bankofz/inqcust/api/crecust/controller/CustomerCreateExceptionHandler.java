package com.bankofz.inqcust.api.crecust.controller;

import com.bankofz.inqcust.api.crecust.domain.CreateCustomerErrorResponse;
import com.bankofz.inqcust.api.crecust.service.CustomerCreateException;
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

@RestControllerAdvice(assignableTypes = CustomerCreateController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CustomerCreateExceptionHandler {

    @ExceptionHandler(CustomerCreateException.class)
    public ResponseEntity<CreateCustomerErrorResponse> handleCustomerCreateException(CustomerCreateException exception) {
        String correlationId = correlationId();
        return ResponseEntity.status(exception.httpStatus())
                .body(error(exception.errorCode(), exception.getMessage(), exception.legacyFailCode(), correlationId));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    public ResponseEntity<CreateCustomerErrorResponse> handleValidationException(Exception exception) {
        String correlationId = correlationId();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(error("ERR-001", "Invalid request payload", " ", correlationId));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CreateCustomerErrorResponse> handleUnexpectedException(Exception exception) {
        String correlationId = correlationId();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error("ERR-999", "Unexpected internal failure", " ", correlationId));
    }

    private String correlationId() {
        String correlationId = MDC.get("correlationId");
        return (correlationId == null || correlationId.isBlank()) ? UUID.randomUUID().toString() : correlationId;
    }

    private CreateCustomerErrorResponse error(String code, String message, String legacyFailCode, String correlationId) {
        return new CreateCustomerErrorResponse(
                new CreateCustomerErrorResponse.ErrorBody(
                        code,
                        message,
                        legacyFailCode,
                        correlationId,
                        Instant.now().toString()
                )
        );
    }
}
