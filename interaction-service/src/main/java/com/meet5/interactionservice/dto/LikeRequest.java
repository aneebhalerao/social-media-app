package com.meet5.interactionservice.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record LikeRequest(
        @NotNull(message = "LikerId is required")
        UUID likerId,
        @NotNull(message = "LikedId is required")
        UUID likedId) {
}
