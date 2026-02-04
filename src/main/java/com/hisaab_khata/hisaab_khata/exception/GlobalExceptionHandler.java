package com.hisaab_khata.hisaab_khata.exception;


import com.hisaab_khata.hisaab_khata.dto.ErrorResponse;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    private ErrorResponse buildError(
            HttpStatus status,
            String message,
            String code,
            String path
    ) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .code(code)
                .path(path)
                .build();
    }

    // -----------------------------
    // RESOURCE NOT FOUND
    // -----------------------------
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException ex,
            WebRequest request
    ) {
        ErrorResponse response = buildError(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                ex.getCode(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    // -----------------------------
    // BUSINESS VALIDATION FAILURES
    // -----------------------------
    @ExceptionHandler(BusinessValidationException.class)
    public ResponseEntity<ErrorResponse> handleBusinessError(
            BusinessValidationException ex,
            WebRequest request
    ) {
        ErrorResponse response = buildError(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                ex.getCode(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // -----------------------------
    // UNAUTHORIZED (JWT/No Login)
    // -----------------------------
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(
            UnauthorizedException ex,
            WebRequest request
    ) {
        ErrorResponse response = buildError(
                HttpStatus.UNAUTHORIZED,
                ex.getMessage(),
                ex.getCode(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    // -----------------------------
    // ACCESS DENIED (Different shop)
    // -----------------------------
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            WebRequest request
    ) {
        ErrorResponse response = buildError(
                HttpStatus.FORBIDDEN,
                ex.getMessage(),
                ex.getCode(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    // -----------------------------
    // @Valid Validation Failures
    // -----------------------------
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            WebRequest request
    ) {
        FieldError fieldError = ex.getBindingResult().getFieldError();

        String message = (fieldError != null)
                ? fieldError.getField() + " " + fieldError.getDefaultMessage()
                : "Validation failed";

        ErrorResponse response = buildError(
                HttpStatus.BAD_REQUEST,
                message,
                "VALIDATION_FAILED",
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // -----------------------------
    // ANY OTHER ERROR
    // -----------------------------
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobal(
            Exception ex,
            WebRequest request
    ) {
        ErrorResponse response = buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getMessage(),
                "INTERNAL_ERROR",
                request.getDescription(false).replace("uri=", "")
        );
        ex.printStackTrace(); // Optional log
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

