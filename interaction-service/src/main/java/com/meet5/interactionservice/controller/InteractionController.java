package com.meet5.interactionservice.controller;

import com.meet5.interactionservice.dto.*;
import com.meet5.interactionservice.service.InteractionService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/interactions")
public class InteractionController {

    private final InteractionService interactionService;
    public InteractionController(InteractionService interactionService) {
        this.interactionService = interactionService;
    }

    @PostMapping("/visit")
    @Operation(
            summary     = "Record a profile visit",
            description = "Records that visitorId visited visitedId. Increments count if visited before."
    )
    public ResponseEntity<VisitResponse> recordVisit(@Valid @RequestBody VisitRequest visitRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(interactionService.recordVisit(visitRequest));
    }

    @PostMapping("/like")
    public ResponseEntity<LikeResponse> recordLike(@Valid @RequestBody LikeRequest likeRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(interactionService.recordLike(likeRequest));
    }

    @GetMapping("/{userId}/visitors")
    @Operation(
            summary     = "Get visitors of a profile",
            description = "Returns paginated list of users who visited this profile, most recent first."
    )
    public ResponseEntity<List<VisitorSummary>> getVisitors(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0")  @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int pageSize) {
        return ResponseEntity.status(HttpStatus.OK).body(interactionService.getVisitors(userId, page, pageSize));
    }

}
