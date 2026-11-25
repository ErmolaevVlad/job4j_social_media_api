package ru.job4j.socialmediaapi.controller.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    record Violation(String field, String message) {
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ProblemDetail handleUnreadableJson() {
        var problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Не удалось прочитать JSON запроса.");
        problem.setProperty("code", "MALFORMED_JSON");
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleArgumentNotValid(MethodArgumentNotValidException ex) {
        var problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Ошибка валидации запроса"
        );
        problem.setProperty("code", "VALIDATION_ERROR");
        var violations = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> new Violation(err.getField(), err.getDefaultMessage()))
                .toList();

        problem.setProperty("violations", violations);
        return problem;
    }
}
