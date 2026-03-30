package com.meet5.interactionservice.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record VisitRequest(
        @NotNull(message = "visitorId is required")
        UUID visitorId,
        @NotNull(message = "visitedId is required")
        UUID visitedId) {
}
