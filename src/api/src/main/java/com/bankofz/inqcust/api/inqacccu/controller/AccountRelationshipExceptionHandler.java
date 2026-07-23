package com.bankofz.inqcust.api.inqacccu.controller;

import com.bankofz.inqcust.api.inqacccu.domain.ApiError;
import com.bankofz.inqcust.api.inqacccu.domain.ValidationError;
import com.bankofz.inqcust.api.inqacccu.exception.RepositoryUnavailableException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice(basePackageClasses = AccountRelationshipController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AccountRelationshipExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleValidation(ConstraintViolationException exception) {
        List<ValidationError> details = exception.getConstraintViolations().stream()
                .map(this::toValidationError)
                .toList();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ApiError(new ApiError.ErrorPayload(
                "VALIDATION_ERROR",
                "Validation failed",
                details
            )));
    }

    @ExceptionHandler(RepositoryUnavailableException.class)
    public ResponseEntity<ApiError> handleRepositoryUnavailable(RepositoryUnavailableException exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ApiError(new ApiError.ErrorPayload(
                "INFRASTRUCTURE_ERROR",
                "Service unavailable due to infrastructure failure",
                null
            )));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ApiError(new ApiError.ErrorPayload(
                "INFRASTRUCTURE_ERROR",
                "Service unavailable due to infrastructure failure",
                null
            )));
    }

    private ValidationError toValidationError(ConstraintViolation<?> violation) {
        String path = violation.getPropertyPath().toString();
        String field = path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
        return new ValidationError(field, violation.getMessage());
    }
}
