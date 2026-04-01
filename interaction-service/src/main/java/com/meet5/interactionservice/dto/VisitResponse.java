package com.meet5.interactionservice.dto;

import java.time.Instant;
import java.util.UUID;

public record VisitResponse(
        UUID visitorId,
        UUID visitedId,
        int visitCount,
        Instant lastVisitAt) {
}
