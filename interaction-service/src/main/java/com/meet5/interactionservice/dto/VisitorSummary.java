package com.meet5.interactionservice.dto;

import java.time.Instant;
import java.util.UUID;

public record VisitorSummary(
        UUID visitorId,
        int visitCount,
        Instant firstVisitedAt,
        Instant lastVisitedAt
) {
}
