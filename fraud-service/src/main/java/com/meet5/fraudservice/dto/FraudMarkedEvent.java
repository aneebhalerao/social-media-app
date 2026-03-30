package com.meet5.fraudservice.dto;

import java.time.Instant;
import java.util.UUID;

// Published to Kafka when user is marked as fraud
// Consumed by interaction-service and api-gateway
public record FraudMarkedEvent(
    UUID userId,
    String reason,
    int actionCount,
    Instant markedAt
) {
    public static final String TOPIC = "fraud.user.marked";
}