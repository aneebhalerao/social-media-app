package com.meet5.fraudservice.controller;

import com.meet5.fraudservice.domain.FraudStatus;
import com.meet5.fraudservice.service.FraudDetectionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@DisplayName("FraudController")
public class FraudControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private FraudDetectionService service;

    @Test
    @DisplayName("GET /api/v1/frauds/{userId}/status — returns CLEAN for clean user")
    public void shouldReturnCleanUserStatus() throws Exception {
        UUID userId = UUID.randomUUID();
        when(service.getStatus(userId)).thenReturn(FraudStatus.CLEAN);

        mockMvc.perform(get("/api/v1/frauds/{userId}/status", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.status").value("CLEAN"))
                .andExpect(jsonPath("$.blocked").value(false))
                .andExpect(jsonPath("$.checkedAt").exists());
    }

    @Test
    @DisplayName("GET /api/v1/frauds/{userId}/status — returns FRAUD for fraud user")
    public void shouldReturnFraudUserStatus() throws Exception {
        UUID userId = UUID.randomUUID();
        when(service.getStatus(userId)).thenReturn(FraudStatus.FRAUD);

        mockMvc.perform(get("/api/v1/frauds/{userId}/status", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.status").value("FRAUD"))
                .andExpect(jsonPath("$.blocked").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/frauds/{userId}/status — returns SUSPECT for suspected user")
    public void shouldReturnSuspectUserStatus() throws Exception {
        UUID userId = UUID.randomUUID();
        when(service.getStatus(userId)).thenReturn(FraudStatus.SUSPECT);

        mockMvc.perform(get("/api/v1/frauds/{userId}/status", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.status").value("SUSPECT"))
                .andExpect(jsonPath("$.blocked").value(false));
    }

    @Test
    @DisplayName("POST /api/v1/frauds/{userId}/mark — manually marks user as fraud")
    public void shouldMarkUserAsFraud() throws Exception {
        UUID userId = UUID.randomUUID();
        when(service.getStatus(userId)).thenReturn(FraudStatus.FRAUD);

        mockMvc.perform(get("/api/v1/frauds/{userId}/status", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FRAUD"))
                .andExpect(jsonPath("$.blocked").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/frauds/{userId}/status — returns 400 for invalid UUID")
    void shouldReturn400ForInvalidUuid() throws Exception {
        mockMvc.perform(get("/api/v1/frauds/{userId}/status", "not-a-uuid"))
                .andExpect(status().isBadRequest());
    }
}
