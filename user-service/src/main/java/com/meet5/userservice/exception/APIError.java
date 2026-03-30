package com.meet5.userservice.exception;


import java.time.Instant;

public record APIError(
            int status,
            String error,
            String message,
            String path,
            Instant timestamp
    ) {}

