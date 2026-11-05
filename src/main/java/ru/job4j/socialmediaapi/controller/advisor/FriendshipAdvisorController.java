package ru.job4j.socialmediaapi.controller.advisor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.job4j.socialmediaapi.exception.DuplicateFriendshipException;
import ru.job4j.socialmediaapi.exception.FriendNotFoundException;

import java.time.Instant;

@RestControllerAdvice
public class FriendshipAdvisorController {

    @ExceptionHandler(FriendNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleFriendNotFound(FriendNotFoundException ex) {
        var errorResponse = new ApiErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "NOT_FOUND",
                ex.getMessage(),
                Instant.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(DuplicateFriendshipException.class)
    public ResponseEntity<Void> handleDuplicateFriendship(DuplicateFriendshipException ex) {
        return ResponseEntity.noContent().build();
    }
}
