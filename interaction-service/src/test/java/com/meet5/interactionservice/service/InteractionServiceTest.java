package com.meet5.interactionservice.service;

import com.meet5.interactionservice.dto.LikeRequest;
import com.meet5.interactionservice.dto.LikeResponse;
import com.meet5.interactionservice.dto.VisitRequest;
import com.meet5.interactionservice.dto.VisitResponse;
import com.meet5.interactionservice.dto.VisitorSummary;
import com.meet5.interactionservice.exception.UserBlockedException;
import com.meet5.interactionservice.kafka.ActionEventPublisher;
import com.meet5.interactionservice.repository.BlockUserRespository;
import com.meet5.interactionservice.repository.LikeRepository;
import com.meet5.interactionservice.repository.VisitRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InteractionService")
class InteractionServiceTest {

    @Mock
    private VisitRepository visitRepository;

    @Mock
    private LikeRepository likeRepository;

    @Mock
    BlockUserRespository blockUserRespository;

    @Mock
    private ActionEventPublisher actionEventPublisher;

    @InjectMocks
    private InteractionService interactionService;

    private final UUID visitorId = UUID.randomUUID();
    private final UUID visitedId = UUID.randomUUID();
    private final UUID likerId   = UUID.randomUUID();
    private final UUID likedId   = UUID.randomUUID();

    // ── recordVisit ───────────────────────────────────────────────

    @Nested
    @DisplayName("recordVisit")
    class RecordVisit {

        @Test
        @DisplayName("should record visit successfully for clean user")
        void shouldRecordVisitForCleanUser() {
            when(blockUserRespository.isBlocked(visitorId)).thenReturn(false);
            when(visitRepository.recordVisits(visitorId, visitedId)).thenReturn(1);

            VisitResponse response = interactionService.recordVisit(
                new VisitRequest(visitorId, visitedId)
            );

            assertThat(response.visitorId()).isEqualTo(visitorId);
            assertThat(response.visitedId()).isEqualTo(visitedId);
            assertThat(response.visitCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("should increment visit count on repeated visit")
        void shouldIncrementVisitCount() {
            when(blockUserRespository.isBlocked(visitorId)).thenReturn(false);
            when(visitRepository.recordVisits(visitorId, visitedId)).thenReturn(3);

            VisitResponse response = interactionService.recordVisit(
                new VisitRequest(visitorId, visitedId)
            );

            assertThat(response.visitCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("should throw UserBlockedException for blocked user")
        void shouldThrowForBlockedUser() {
            when(blockUserRespository.isBlocked(visitorId)).thenReturn(true);

            assertThatThrownBy(() ->
                interactionService.recordVisit(new VisitRequest(visitorId, visitedId))
            )
                .isInstanceOf(UserBlockedException.class)
                .hasMessageContaining(visitorId.toString());
        }

        @Test
        @DisplayName("should publish action event after visit")
        void shouldPublishActionEvent() {
            when(blockUserRespository.isBlocked(visitorId)).thenReturn(false);
            when(visitRepository.recordVisits(visitorId, visitedId)).thenReturn(1);

            interactionService.recordVisit(new VisitRequest(visitorId, visitedId));

            verify(actionEventPublisher).publishVisit(visitorId);
        }

        @Test
        @DisplayName("should NOT publish action event when user is blocked")
        void shouldNotPublishWhenBlocked() {
            when(blockUserRespository.isBlocked(visitorId)).thenReturn(true);

            assertThatThrownBy(() ->
                interactionService.recordVisit(new VisitRequest(visitorId, visitedId))
            ).isInstanceOf(UserBlockedException.class);

            verifyNoInteractions(actionEventPublisher);
        }

        @Test
        @DisplayName("should NOT call repository when user is blocked")
        void shouldNotCallRepositoryWhenBlocked() {
            when(blockUserRespository.isBlocked(visitorId)).thenReturn(true);

            assertThatThrownBy(() ->
                interactionService.recordVisit(new VisitRequest(visitorId, visitedId))
            ).isInstanceOf(UserBlockedException.class);

            verify(visitRepository, never()).recordVisits(any(), any());
        }
    }

    // ── recordLike ────────────────────────────────────────────────

    @Nested
    @DisplayName("recordLike")
    class RecordLike {

        @Test
        @DisplayName("should record like successfully for clean user")
        void shouldRecordLikeForCleanUser() {
            when(blockUserRespository.isBlocked(likerId)).thenReturn(false);
            when(likeRepository.recordLikes(likerId, likedId)).thenReturn(true);

            LikeResponse response = interactionService.recordLike(
                new LikeRequest(likerId, likedId)
            );

            assertThat(response.likerId()).isEqualTo(likerId);
            assertThat(response.likedId()).isEqualTo(likedId);
            assertThat(response.isNew()).isTrue();
        }

        @Test
        @DisplayName("should return isNew false for duplicate like")
        void shouldReturnIsNewFalseForDuplicate() {
            when(blockUserRespository.isBlocked(likerId)).thenReturn(false);
            when(likeRepository.recordLikes(likerId, likedId)).thenReturn(false);

            LikeResponse response = interactionService.recordLike(
                new LikeRequest(likerId, likedId)
            );

            assertThat(response.isNew()).isFalse();
        }

        @Test
        @DisplayName("should throw UserBlockedException for blocked user")
        void shouldThrowForBlockedUser() {
            when(blockUserRespository.isBlocked(likerId)).thenReturn(true);

            assertThatThrownBy(() ->
                interactionService.recordLike(new LikeRequest(likerId, likedId))
            )
                .isInstanceOf(UserBlockedException.class)
                .hasMessageContaining(likerId.toString());
        }

        @Test
        @DisplayName("should publish action event after like")
        void shouldPublishActionEvent() {
            when(blockUserRespository.isBlocked(likerId)).thenReturn(false);
            when(likeRepository.recordLikes(likerId, likedId)).thenReturn(true);

            interactionService.recordLike(new LikeRequest(likerId, likedId));

            verify(actionEventPublisher).publishLike(likerId);
        }
    }

    // ── getVisitors ───────────────────────────────────────────────

    @Nested
    @DisplayName("getVisitors")
    class GetVisitors {

        @Test
        @DisplayName("should return visitors list")
        void shouldReturnVisitorsList() {
            UUID userId = UUID.randomUUID();
            List<VisitorSummary> expected = List.of(
                new VisitorSummary(UUID.randomUUID(), 3, Instant.now(), Instant.now()),
                new VisitorSummary(UUID.randomUUID(), 1, Instant.now(), Instant.now())
            );

            when(visitRepository.findVisitors(userId, 20, 0))
                .thenReturn(expected);

            List<VisitorSummary> result = interactionService.getVisitors(userId, 0, 20);

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("should calculate correct offset from page and size")
        void shouldCalculateCorrectOffset() {
            UUID userId = UUID.randomUUID();
            when(visitRepository.findVisitors(userId, 10, 20))
                .thenReturn(List.of());

            interactionService.getVisitors(userId, 2, 10); // page=2, size=10 → offset=20

            verify(visitRepository).findVisitors(userId, 10, 20);
        }

        @Test
        @DisplayName("should return empty list when no visitors")
        void shouldReturnEmptyList() {
            UUID userId = UUID.randomUUID();
            when(visitRepository.findVisitors(any(), anyInt(), anyInt()))
                .thenReturn(List.of());

            List<VisitorSummary> result = interactionService.getVisitors(userId, 0, 20);

            assertThat(result).isEmpty();
        }
    }
}