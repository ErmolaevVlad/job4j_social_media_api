package ru.job4j.socialmediaapi.controller.advisor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.job4j.socialmediaapi.exception.*;

import java.time.Instant;

@RestControllerAdvice
public class OfferFriendshipAdvisorController {

    @ExceptionHandler(SelfFriendshipException.class)
    public ResponseEntity<ApiErrorResponse> handleSelfFriendship(SelfFriendshipException ex) {
        var errorResponse = new ApiErrorResponse(
                HttpStatus.CONFLICT.value(),
                "CONFLICT",
                ex.getMessage(),
                Instant.now()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(DuplicateFriendshipRequestException.class)
    public ResponseEntity<Void> handleDuplicateFriendshipRequest() {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @ExceptionHandler(OfferFriendshipNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleOfferFriendshipNotFound(OfferFriendshipNotFoundException ex) {
        var errorResponse = new ApiErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "NOT_FOUND",
                ex.getMessage(),
                Instant.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(OfferFriendshipAlreadyProcessedException.class)
    public ResponseEntity<ApiErrorResponse> offerFriendshipAlreadyProcessed(OfferFriendshipAlreadyProcessedException ex) {
        var errorResponse = new ApiErrorResponse(
                HttpStatus.CONFLICT.value(),
                "CONFLICT",
                ex.getMessage(),
                Instant.now()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            jakarta.validation.ConstraintViolationException ex) {
        var errorResponse = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "BAD_REQUEST",
                ex.getMessage(),
                Instant.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}