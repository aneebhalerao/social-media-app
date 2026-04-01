package com.meet5.userservice.dto;

public record BulkInsertResponse(
    int inserted,
    int skipped,
    int total,
    long durationMs
) {}