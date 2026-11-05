package ru.job4j.socialmediaapi.controller.advisor;

import java.time.Instant;

public record ApiErrorResponse(
        int status,
        String error,
        String message,
        Instant timestamp
) {
}
