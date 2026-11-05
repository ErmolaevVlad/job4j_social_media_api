package ru.job4j.socialmediaapi.controller.advisor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.job4j.socialmediaapi.exception.DuplicateSubscriptionException;
import ru.job4j.socialmediaapi.exception.SelfSubscriptionException;
import ru.job4j.socialmediaapi.exception.SubscriptionNotFoundException;

import java.time.Instant;

@RestControllerAdvice
public class SubscriptionAdvisorController {
    @ExceptionHandler(DuplicateSubscriptionException.class)
    public ResponseEntity<Void> handleDuplicateFriendshipRequest() {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @ExceptionHandler(SelfSubscriptionException.class)
    public ResponseEntity<ApiErrorResponse> handleSelfSubscription(SelfSubscriptionException ex) {
        var errorResponse = new ApiErrorResponse(
                HttpStatus.CONFLICT.value(),
                "CONFLICT",
                ex.getMessage(),
                Instant.now()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(SubscriptionNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleSubscriptionNotFound(SubscriptionNotFoundException ex) {
        var errorResponse = new ApiErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "NOT_FOUND",
                ex.getMessage(),
                Instant.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
}