package com.meet5.userservice.domain;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class User {
    private UUID id;

    @NotBlank
    @Size(max = 50)
    private String username;

    @NotBlank
    @Size(max = 100)
    private String name;

    @Min(15)
    @Max(120)
    private int age;

    @NotNull
    private UserStatus status = UserStatus.ACTIVE;

    private Map<@Size(max = 50) String, Object> extraFields;
    private Instant createdAt;
    private Instant updatedAt;

}
