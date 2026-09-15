package com.aerometal.assignmentmanager.exception;

import java.time.Instant;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.persistence.OptimisticLockException;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /*
     * Handles an outdated version detected explicitly by the service.
     *
     * Example:
     * request.version() != entity.getVersion()
     */
    @ExceptionHandler(StaleEntityException.class)
    public ResponseEntity<ApiError> handleStaleEntityException(
            StaleEntityException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request
        );
    }

    /*
     * Handles optimistic-locking exceptions translated by Spring.
     *
     * This also handles ObjectOptimisticLockingFailureException because
     * it extends OptimisticLockingFailureException.
     */
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ApiError> handleOptimisticLockingFailureException(
            OptimisticLockingFailureException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.CONFLICT,
                optimisticLockingMessage(),
                request
        );
    }

    /*
     * Handles an optimistic-locking exception exposed directly by JPA.
     */
    @ExceptionHandler(OptimisticLockException.class)
    public ResponseEntity<ApiError> handleOptimisticLockException(
            OptimisticLockException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.CONFLICT,
                optimisticLockingMessage(),
                request
        );
    }

    /*
     * Handles invalid update requests, such as a missing version.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgumentException(
            IllegalArgumentException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request
        );
    }

    private ResponseEntity<ApiError> buildResponse(
            HttpStatus status,
            String message,
            HttpServletRequest request
    ) {
        ApiError apiError = createApiError(
                status,
                message,
                request
        );

        return ResponseEntity
                .status(status)
                .body(apiError);
    }

    private ApiError createApiError(
            HttpStatus status,
            String message,
            HttpServletRequest request
    ) {
        return new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI()
        );
    }

    private String optimisticLockingMessage() {
        return "This record was modified by another user. "
                + "Reload the latest data before saving again.";
    }
}