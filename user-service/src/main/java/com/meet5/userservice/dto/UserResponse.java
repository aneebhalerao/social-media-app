package com.meet5.userservice.dto;

import com.meet5.userservice.domain.UserStatus;
import lombok.Data;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String username,
        int age,
        UserStatus status,
        Map<String, Object> extraFields,
        Instant createdAt,
        Instant updatedAt
) {}
