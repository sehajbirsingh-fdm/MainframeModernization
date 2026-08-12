package com.bankofz.mainframemodernization.inqcust.controller;

import com.bankofz.mainframemodernization.inqcust.domain.ErrorResponse;
import com.bankofz.mainframemodernization.inqcust.domain.FieldErrorResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice(assignableTypes = CustomerInquiryController.class)
public class GlobalExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException exception) {
        List<FieldErrorResponse> fieldErrors = exception.getConstraintViolations().stream()
                .map(this::toFieldError)
                .toList();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        "VALIDATION_ERROR",
                        "Request validation failed",
                        fieldErrors
                ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        "VALIDATION_ERROR",
                        exception.getMessage(),
                        List.of()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                        "INTERNAL_ERROR",
                        "Unexpected system error",
                        List.of()
                ));
    }

    private FieldErrorResponse toFieldError(ConstraintViolation<?> violation) {
        String path = violation.getPropertyPath().toString();
        String fieldName = path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
        return new FieldErrorResponse(fieldName, violation.getMessage());
    }
}
