package com.meet5.interactionservice.repository;

import com.meet5.interactionservice.dto.VisitorSummary;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({VisitRepository.class, LikeRepository.class, BlockUserRespository.class})
@DisplayName("InteractionRepository")
class InteractionRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("interaction_test")
            .withUsername("postgres")
            .withPassword("postgres");

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private VisitRepository visitRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private BlockUserRespository blockUserRespository;

    @Test
    @DisplayName("should create new visit record with count 1")
    void shouldCreateNewVisit() {
        UUID visitorId = UUID.randomUUID();
        UUID visitedId = UUID.randomUUID();

        int count = visitRepository.recordVisits(visitorId, visitedId);

        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("should increment count on repeated visit")
    void shouldIncrementOnRepeatVisit() {
        UUID visitorId = UUID.randomUUID();
        UUID visitedId = UUID.randomUUID();

        visitRepository.recordVisits(visitorId, visitedId);
        visitRepository.recordVisits(visitorId, visitedId);
        int count = visitRepository.recordVisits(visitorId, visitedId);

        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("should track different pairs independently")
    void shouldTrackDifferentPairsIndependently() {
        UUID visitor1 = UUID.randomUUID();
        UUID visitor2 = UUID.randomUUID();
        UUID visited = UUID.randomUUID();

        int count1 = visitRepository.recordVisits(visitor1, visited);
        int count2 = visitRepository.recordVisits(visitor2, visited);

        assertThat(count1).isEqualTo(1);
        assertThat(count2).isEqualTo(1);
    }

    @Test
    @DisplayName("should return visitors sorted by most recent first")
    void shouldReturnVisitorsSortedByRecency() throws InterruptedException {
        UUID visitedId = UUID.randomUUID();
        UUID visitor1 = UUID.randomUUID();
        UUID visitor2 = UUID.randomUUID();

        visitRepository.recordVisits(visitor1, visitedId);
        Thread.sleep(10); // ensure different timestamps
        visitRepository.recordVisits(visitor2, visitedId);

        List<VisitorSummary> visitors =
                visitRepository.findVisitors(visitedId, 10, 0);

        assertThat(visitors).hasSize(2);
        // visitor2 visited last — should be first
        assertThat(visitors.get(0).visitorId()).isEqualTo(visitor2);
    }

    @Test
    @DisplayName("should return empty list when no visitors")
    void shouldReturnEmptyListWhenNoVisitors() {
        List<VisitorSummary> visitors =
                visitRepository.findVisitors(UUID.randomUUID(), 10, 0);

        assertThat(visitors).isEmpty();
    }

    @Test
    @DisplayName("should respect limit parameter")
    void shouldRespectLimit() {
        UUID visitedId = UUID.randomUUID();
        for (int i = 0; i < 5; i++) {
            visitRepository.recordVisits(UUID.randomUUID(), visitedId);
        }

        List<VisitorSummary> visitors =
                visitRepository.findVisitors(visitedId, 3, 0);

        assertThat(visitors).hasSize(3);
    }

    @Test
    @DisplayName("should respect offset for pagination")
    void shouldRespectOffset() {
        UUID visitedId = UUID.randomUUID();
        for (int i = 0; i < 5; i++) {
            visitRepository.recordVisits(UUID.randomUUID(), visitedId);
        }

        List<VisitorSummary> page1 =
                visitRepository.findVisitors(visitedId, 3, 0);
        List<VisitorSummary> page2 =
                visitRepository.findVisitors(visitedId, 3, 3);

        assertThat(page1).hasSize(3);
        assertThat(page2).hasSize(2);
        // no overlap between pages
        assertThat(page1).doesNotContainAnyElementsOf(page2);
    }

    @Test
    @DisplayName("should return true for new like")
    void shouldReturnTrueForNewLike() {
        boolean isNew = likeRepository.recordLikes(
                UUID.randomUUID(), UUID.randomUUID()
        );
        assertThat(isNew).isTrue();
    }

    @Test
    @DisplayName("should return false for duplicate like")
    void shouldReturnFalseForDuplicateLike() {
        UUID likerId = UUID.randomUUID();
        UUID likedId = UUID.randomUUID();

        likeRepository.recordLikes(likerId, likedId);
        boolean isNew = likeRepository.recordLikes(likerId, likedId);

        assertThat(isNew).isFalse();
    }

    @Test
    @DisplayName("should allow same user to like different profiles")
    void shouldAllowLikingDifferentProfiles() {
        UUID likerId = UUID.randomUUID();

        boolean like1 = likeRepository.recordLikes(likerId, UUID.randomUUID());
        boolean like2 = likeRepository.recordLikes(likerId, UUID.randomUUID());

        assertThat(like1).isTrue();
        assertThat(like2).isTrue();
    }

    @Test
    @DisplayName("should return false for non-blocked user")
    void shouldReturnFalseForCleanUser() {
        boolean blocked = blockUserRespository.isBlocked(UUID.randomUUID());
        assertThat(blocked).isFalse();
    }

    @Test
    @DisplayName("should return true after user is blocked")
    void shouldReturnTrueAfterBlocking() {
        UUID userId = UUID.randomUUID();
        blockUserRespository.blockUser(userId);

        boolean blocked = blockUserRespository.isBlocked(userId);

        assertThat(blocked).isTrue();
    }

    @Test
    @DisplayName("should not throw when blocking same user twice")
    void shouldNotThrowOnDuplicateBlock() {
        UUID userId = UUID.randomUUID();

        blockUserRespository.blockUser(userId);
        blockUserRespository.blockUser(userId); // should be idempotent

        assertThat(blockUserRespository.isBlocked(userId)).isTrue();
    }

    @Test
    @DisplayName("blocking one user should not affect others")
    void shouldNotAffectOtherUsers() {
        UUID blockedUser = UUID.randomUUID();
        UUID cleanUser = UUID.randomUUID();

        blockUserRespository.blockUser(blockedUser);

        assertThat(blockUserRespository.isBlocked(blockedUser)).isTrue();
        assertThat(blockUserRespository.isBlocked(cleanUser)).isFalse();
    }
}