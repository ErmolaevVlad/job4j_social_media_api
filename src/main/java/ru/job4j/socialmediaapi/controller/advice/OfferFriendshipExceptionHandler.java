package ru.job4j.socialmediaapi.controller.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.job4j.socialmediaapi.controller.OfferFriendshipController;
import ru.job4j.socialmediaapi.controller.dto.ApiErrorCode;
import ru.job4j.socialmediaapi.exception.*;

@RestControllerAdvice(assignableTypes = OfferFriendshipController.class)
public class OfferFriendshipExceptionHandler {

    @ExceptionHandler(SelfFriendshipException.class)
    public ProblemDetail handleSelfFriendship(SelfFriendshipException ex) {
        return problem(HttpStatus.CONFLICT, ex.getMessage(), ApiErrorCode.SELF_FRIEND_REQUEST);
    }

    @ExceptionHandler(OfferFriendshipNotFoundException.class)
    public ProblemDetail handleOfferFriendshipNotFound(OfferFriendshipNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, ex.getMessage(), ApiErrorCode.FRIEND_REQUEST_NOT_FOUND);
    }

    @ExceptionHandler({
            OfferFriendshipAlreadyProcessedException.class,
            OfferFriendshipAlreadyRejectedException.class
    })
    public ProblemDetail handleRejectedOrProcessed(RuntimeException ex) {
        return problem(HttpStatus.CONFLICT, ex.getMessage(), ApiErrorCode.FRIEND_REQUEST_REJECTED);
    }

    @ExceptionHandler({
            DuplicateFriendshipRequestException.class,
            OfferFriendshipAlreadyAcceptException.class,
            DuplicateFriendshipException.class
    })
    public ResponseEntity<Void> handleNoContent() {
        return ResponseEntity.noContent().build();
    }

    private ProblemDetail problem(HttpStatus status,
                                  String detail, ApiErrorCode code) {
        var problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setProperty("code", code.getValue());
        return problem;
    }
}