package ru.job4j.socialmediaapi.controller.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.job4j.socialmediaapi.controller.OfferFriendshipController;
import ru.job4j.socialmediaapi.controller.dto.ApiErrorCode;
import ru.job4j.socialmediaapi.exception.*;

import java.net.URI;
import java.time.Instant;

@RestControllerAdvice(assignableTypes = OfferFriendshipController.class)
public class OfferFriendshipExceptionHandler {

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

    @ExceptionHandler(DuplicateFriendshipException.class)
    public ResponseEntity<Void> handleDuplicateFriendship(DuplicateFriendshipException ex) {
        return ResponseEntity.noContent().build();
    }

    private ProblemDetail problem(HttpStatus status, URI type, String title,
                                  String detail, ApiErrorCode code) {
        var problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(type);
        problem.setTitle(title);
        problem.setProperty("code", code.getValue());
        return problem;
    }
}