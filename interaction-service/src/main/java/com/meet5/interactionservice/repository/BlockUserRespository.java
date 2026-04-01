package com.meet5.interactionservice.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
public class BlockUserRespository {

    JdbcClient jdbcClient;
    public BlockUserRespository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Transactional
    public void blockUser(UUID userId) {
        jdbcClient.sql("""
                INSERT INTO blocked_users(user_id, blocked_at) VALUES 
                (:userId, now())
                ON CONFLICT (user_id) DO NOTHING
                """)
                .param("userId", userId)
                .update();
    }

    @Transactional(readOnly = true)
    public boolean isBlocked(UUID userId) {
        return jdbcClient.sql("""
                SELECT COUNT(*) FROM blocked_users WHERE user_id = :userId
                """)
                .param("userId", userId)
                .query(Integer.class)
                .single() > 0;
    }
}
