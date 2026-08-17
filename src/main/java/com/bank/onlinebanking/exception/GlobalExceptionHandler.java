package com.bank.onlinebanking.exception;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import jakarta.validation.ConstraintViolationException;

/**
 * Central place that converts every thrown exception into a consistent
 * JSON error response. Internal details and stack traces are never exposed
 * to clients.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // =====================================================
    // 400 BAD REQUEST
    // =====================================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException ex,
            WebRequest request) {

        Map<String, String> errors = new HashMap<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        ApiError body = ApiError.of(
                HttpStatus.BAD_REQUEST.value(),
                "BAD_REQUEST",
                "Validation failed",
                pathOf(request)
        );
        body.setValidationErrors(errors);

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(
            ConstraintViolationException ex,
            WebRequest request) {

        return build(HttpStatus.BAD_REQUEST,
                "Validation failed", ex.getMessage(), request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleNotReadable(
            HttpMessageNotReadableException ex,
            WebRequest request) {

        return build(HttpStatus.BAD_REQUEST,
                "Malformed request body", "Invalid JSON or field format",
                request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParam(
            MissingServletRequestParameterException ex,
            WebRequest request) {

        return build(HttpStatus.BAD_REQUEST,
                "BAD_REQUEST",
                "Required parameter '" + ex.getParameterName() + "' is missing",
                request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException ex,
            WebRequest request) {

        return build(HttpStatus.METHOD_NOT_ALLOWED,
                "METHOD_NOT_ALLOWED",
                "Request method not supported for this endpoint",
                request);
    }

    @ExceptionHandler(org.springframework.web.HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiError> handleMediaTypeNotSupported(
            org.springframework.web.HttpMediaTypeNotSupportedException ex,
            WebRequest request) {

        return build(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "UNSUPPORTED_MEDIA_TYPE",
                "Content-Type not supported for this endpoint",
                request);
    }

    @ExceptionHandler(InvalidTransactionException.class)
    public ResponseEntity<ApiError> handleInvalidTransaction(
            InvalidTransactionException ex,
            WebRequest request) {

        return build(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage(),
                request);
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ApiError> handleInsufficientBalance(
            InsufficientBalanceException ex,
            WebRequest request) {

        return build(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage(),
                request);
    }

    @ExceptionHandler(AccountNotActiveException.class)
    public ResponseEntity<ApiError> handleAccountNotActive(
            AccountNotActiveException ex,
            WebRequest request) {

        return build(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage(),
                request);
    }
// =====================================================
    // 401 UNAUTHORIZED
    // =====================================================

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthentication(
            AuthenticationException ex,
            WebRequest request) {

        return build(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED",
                "Invalid email or password", request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(
            BadCredentialsException ex,
            WebRequest request) {

        return build(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED",
                "Invalid email or password", request);
    }

    // =====================================================
    // 403 FORBIDDEN
    // =====================================================

    @ExceptionHandler(UnauthorizedOperationException.class)
    public ResponseEntity<ApiError> handleUnauthorizedOperation(
            UnauthorizedOperationException ex,
            WebRequest request) {

        return build(HttpStatus.FORBIDDEN, "FORBIDDEN", ex.getMessage(),
                request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(
            AccessDeniedException ex,
            WebRequest request) {

        return build(HttpStatus.FORBIDDEN, "FORBIDDEN",
                "You do not have permission to access this resource",
                request);
    }

    // =====================================================
    // 404 NOT FOUND
    // =====================================================

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(
            ResourceNotFoundException ex,
            WebRequest request) {

        return build(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(),
                request);
    }

    // =====================================================
    // 409 CONFLICT
    // =====================================================

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiError> handleDuplicate(
            DuplicateResourceException ex,
            WebRequest request) {

        return build(HttpStatus.CONFLICT, "CONFLICT", ex.getMessage(),
                request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(
            DataIntegrityViolationException ex,
            WebRequest request) {

        return build(HttpStatus.CONFLICT, "CONFLICT",
                "Resource already exists or violates a unique constraint",
                request);
    }

    // =====================================================
    // 500 INTERNAL SERVER ERROR
    // =====================================================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(
            Exception ex,
            WebRequest request) {

        // Log full details server-side only.
        log.error("Unhandled exception on {}",
                request.getDescription(false), ex);

        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred. Please try again later.",
                request);
    }

    // =====================================================
    // HELPERS
    // =====================================================

    private ResponseEntity<ApiError> build(
            HttpStatus status,
            String error,
            String message,
            WebRequest request) {

        ApiError body = ApiError.of(
                status.value(), error, message, pathOf(request));

        return ResponseEntity.status(status).body(body);
    }

    private String pathOf(WebRequest request) {
        return request.getDescription(false)
                .replace("uri=", "");
    }
}