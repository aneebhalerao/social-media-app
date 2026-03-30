package com.meet5.userservice.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meet5.userservice.domain.User;
import com.meet5.userservice.domain.UserStatus;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.meet5.userservice.util.CommonUtil.fromJson;
import static com.meet5.userservice.util.CommonUtil.toJson;

@Repository
@AllArgsConstructor
public class UserRepository {

    private final JdbcClient jdbcClient;


    @Transactional
    public User insert(User user) {
        UUID uuid = UUID.randomUUID();
        Instant now = Instant.now();

        jdbcClient.sql("""
                        INSERT INTO users(id, name, username, age, status, extra_fields, created_at, updated_at) VALUES 
                        (:id, :name, :username, :age, :status, CAST(:extraFields AS jsonb), :createdAt, :updatedAt) 
                        ON CONFLICT (username) DO NOTHING """)
                .param("id", uuid)
                .param("name", user.getName())
                .param("username", user.getUsername())
                .param("age", user.getAge())
                .param("status", UserStatus.ACTIVE.name())
                .param("extraFields", toJson(user.getExtraFields()))
                .param("createdAt", Timestamp.from(now))
                .param("updatedAt", Timestamp.from(now))
                .update();

        return user.builder()
                .id(uuid)
                .name(user.getName())
                .username(user.getUsername())
                .status(UserStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public Optional<User> findById(UUID id) {
        return jdbcClient.sql("""
                        SELECT id, name, username, age, extra_fields, status, created_at, updated_at FROM users 
                        WHERE id = :id
                        """)
                .param("id", id)
                .query((rs, rowNum) -> mapRow(rs))
                .optional();
    }

    public Optional<User> findByUsername(String username) {
        return jdbcClient.sql("""
                        SELECT id, name, username, age, extra_fields, status, created_at, updated_at FROM users 
                        WHERE username = :username
                        """)
                .param("username", username)
                .query((rs, rowNum) -> mapRow(rs))
                .optional();
    }

    @Transactional
    public void updateStatus(UUID id, String status) {
        jdbcClient.sql("""
                        UPDATE users SET status = :status 
                        where id = :id
                        """)
                .param("status", status)
                .param("id", id)
                .update();
    }

    public int bulkInsert(List<User> users) {
        if (users == null || users.isEmpty()) return 0;

        int count = 0;
        Instant now = Instant.now();
        for (User user : users) {
            count += jdbcClient.sql("""
                            INSERT INTO users(id, name, username, age, status, extra_fields, created_at, updated_at) VALUES 
                            (:id, :name, :username, :age, :status, :extraFields::jsonb, :createdAt, :updatedAt)) 
                            ON CONFLICT (username) DO NOTHING """)
                    .param("id", UUID.randomUUID())
                    .param("name", user.getName())
                    .param("username", user.getUsername())
                    .param("age", user.getAge())
                    .param("status", UserStatus.ACTIVE.name())
                    .param("extraFields", toJson(user.getExtraFields()))
                    .param("createdAt", now)
                    .param("updatedAt", now)
                    .update();
        }
        return count;
    }

    private User mapRow(ResultSet rs) throws SQLException {
        return User.builder()
                .id(UUID.fromString(rs.getString("id")))
                .name(rs.getString("name"))
                .username(rs.getString("username"))
                .age(rs.getInt("age"))
                .extraFields(fromJson(rs.getString("extra_fields")))
                .status(UserStatus.valueOf(rs.getString("status")))
                .createdAt(rs.getTimestamp("created_at").toInstant())
                .updatedAt(rs.getTimestamp("updated_at").toInstant())
                .build();
    }
}
