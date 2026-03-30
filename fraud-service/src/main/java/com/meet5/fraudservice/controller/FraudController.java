package com.meet5.fraudservice.controller;

import com.meet5.fraudservice.dto.FraudStatusResponse;
import com.meet5.fraudservice.service.FraudDetectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fraud")
@Tag(name = "Fraud", description = "Fraud detection and status")
public class FraudController {
    private final FraudDetectionService fraudDetectionService;

    public FraudController(FraudDetectionService fraudDetectionService) {
        this.fraudDetectionService = fraudDetectionService;
    }

    @GetMapping("/{userId}/status")
    @Operation(
            summary     = "Get fraud status for a user",
            description = "Returns current fraud status. Used by api-gateway to block requests."
    )
    public ResponseEntity<FraudStatusResponse> getStatus(@PathVariable UUID userId) {
        var status = fraudDetectionService.getStatus(userId);
        return ResponseEntity.ok(new FraudStatusResponse(
                userId,
                status,
                status.isBlocked(),
                Instant.now()
        ));
    }

    @PostMapping("/{userId}/mark")
    @Operation(
            summary     = "Manually mark a user as fraud",
            description = "Admin endpoint to manually flag a user. Publishes fraud.user.marked event."
    )
    public ResponseEntity<FraudStatusResponse> markFraud(@PathVariable UUID userId) {
        fraudDetectionService.markAsFraud(userId, 0);
        var status = fraudDetectionService.getStatus(userId);
        return ResponseEntity.ok(new FraudStatusResponse(
                userId,
                status,
                status.isBlocked(),
                Instant.now()
        ));
    }
}
