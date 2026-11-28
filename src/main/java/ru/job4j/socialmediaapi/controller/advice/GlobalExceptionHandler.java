package ru.job4j.socialmediaapi.controller.advice;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.job4j.socialmediaapi.controller.dto.ApiErrorCode;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private record Violation(String field, String message) {
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ProblemDetail handleUnreadableJson() {
        var problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Не удалось прочитать JSON запроса.");
        problem.setProperty("code", ApiErrorCode.MALFORMED_JSON.getValue());
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleArgumentNotValid(MethodArgumentNotValidException ex) {
        var problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Ошибка валидации запроса"
        );
        problem.setProperty("code", ApiErrorCode.VALIDATION_ERROR.getValue());
        var violations = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> new Violation(err.getField(), err.getDefaultMessage()))
                .toList();

        problem.setProperty("violations", violations);
        return problem;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex) {
        var problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Ошибка валидации параметров запроса"
        );
        problem.setProperty("code", ApiErrorCode.VALIDATION_ERROR.getValue());
        var violations = ex.getConstraintViolations().stream()
                .map(v -> new Violation(
                        extractFieldName(v.getPropertyPath().toString()),
                        v.getMessage()
                ))
                .toList();

        problem.setProperty("violations", violations);
        return problem;
    }

    private String extractFieldName(String propertyPath) {
        int lastDot = propertyPath.lastIndexOf('.');
        return lastDot != -1 ? propertyPath.substring(lastDot + 1) : propertyPath;
    }
}
