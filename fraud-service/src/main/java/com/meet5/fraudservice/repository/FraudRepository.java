package com.meet5.fraudservice.repository;

import com.meet5.fraudservice.domain.FraudEvent;
import com.meet5.fraudservice.domain.FraudStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public class FraudRepository {

    private final JdbcClient jdbcClient;

    public FraudRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Transactional(readOnly = true)
    public Optional<FraudStatus> findStatusByUserId(UUID userId) {
        return jdbcClient.sql("""
                SELECT status FROM fraud_status WHERE 
                user_id = :userId
                """)
                .param("userId", userId)
                .query((rs, rowNum) -> FraudStatus.valueOf(rs.getString("status")))
                .optional();
    }

    @Transactional
    public void upsertStatus(UUID userId, FraudStatus status) {
        jdbcClient.sql("""
                INSERT INTO fraud_status (user_id, status, updated_at)
                VALUES (:userId, :status, :now)
                ON CONFLICT (user_id)
                DO UPDATE SET
                    status     = EXCLUDED.status,
                    updated_at = EXCLUDED.updated_at
                """)
                .param("userId", userId)
                .param("status", status.name())
                .param("now",    Timestamp.from(Instant.now()))
                .update();
    }

    @Transactional
    public void logFraudEvent(FraudEvent event) {
        jdbcClient.sql("""
                INSERT INTO fraud_events
                    (id, user_id, reason, action_count, detected_at)
                VALUES
                    (:id, :userId, :reason, :actionCount, :detectedAt)
                """)
                .param("id",          UUID.randomUUID())
                .param("userId",      event.getUserId())
                .param("reason",      event.getReason())
                .param("actionCount", event.getActionCount())
                .param("detectedAt",  Timestamp.from(event.getDetectedAt()))
                .update();
    }

}
