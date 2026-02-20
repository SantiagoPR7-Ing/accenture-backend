package com.sprietogo.accenturebackend.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ValidationErrorResponse>> handleValidation(WebExchangeBindException ex) {

        List<ValidationFieldError> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toFieldError)
                .toList();

        ValidationErrorResponse body = new ValidationErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_ERROR",
                "Validation error",
                errors,
                OffsetDateTime.now().toString()
        );

        return Mono.just(ResponseEntity.badRequest().body(body));
    }

    private ValidationFieldError toFieldError(FieldError fe) {
        return new ValidationFieldError(
                fe.getField(),
                fe.getDefaultMessage()
        );
    }


    @ExceptionHandler(ApiException.class)
    public Mono<ResponseEntity<ApiErrorResponse>> handleApiException(ApiException ex) {
        ApiErrorResponse body = new ApiErrorResponse(
                ex.getStatus().value(),
                ex.getCode(),
                ex.getMessage(),
                OffsetDateTime.now().toString()
        );
        return Mono.just(ResponseEntity.status(ex.getStatus()).body(body));
    }


    public record ValidationErrorResponse(
            int status,
            String code,
            String message,
            List<ValidationFieldError> errors,
            String timestamp
    ) {}

    public record ValidationFieldError(String field, String message) {}

    public record ApiErrorResponse(int status, String code, String message, String timestamp) {}
}
