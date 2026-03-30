package com.meet5.interactionservice.dto;

import java.time.Instant;
import java.util.UUID;

public record LikeResponse(
        UUID likerId,
        UUID likedId,
        boolean isNew,
        Instant likedAt) {
}
