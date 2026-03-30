package com.meet5.fraudservice.dto;

import com.meet5.fraudservice.domain.FraudStatus;
import java.time.Instant;
import java.util.UUID;

public record FraudStatusResponse(
    UUID userId,
    FraudStatus status,
    boolean blocked,
    Instant checkedAt
) {}