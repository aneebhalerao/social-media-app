package com.meet5.fraudservice.dto;

import java.util.UUID;

// Consumed from Kafka — sent by interaction-service
// when a user performs a visit or like
public record ActionEvent(
        UUID userId,
        String actionType  // "VISIT" or "LIKE"
) {
}

