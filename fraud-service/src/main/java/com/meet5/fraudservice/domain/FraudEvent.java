package com.meet5.fraudservice.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudEvent {
    private UUID id;
    private UUID userId;
    private String reason;
    private int actionCount;
    private Instant detectedAt;
}