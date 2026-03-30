package com.meet5.interactionservice.service;

import com.meet5.interactionservice.dto.*;
import com.meet5.interactionservice.exception.UserBlockedException;
import com.meet5.interactionservice.kafka.ActionEventPublisher;
import com.meet5.interactionservice.repository.BlockUserRespository;
import com.meet5.interactionservice.repository.LikeRepository;
import com.meet5.interactionservice.repository.VisitRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class InteractionService {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private final LikeRepository likeRepository;
    private final VisitRepository visitRepository;
    private final BlockUserRespository blockUserRespository;
    private final ActionEventPublisher actionEventPublisher;
    public InteractionService(LikeRepository likeRepository, VisitRepository visitRepository,
                              BlockUserRespository blockUserRespository, ActionEventPublisher actionEventPublisher) {
        this.likeRepository = likeRepository;
        this.visitRepository = visitRepository;
        this.blockUserRespository = blockUserRespository;
        this.actionEventPublisher = actionEventPublisher;
    }

    @Transactional
    public VisitResponse recordVisit(VisitRequest request) {
        checkIfNotBlocked(request.visitorId());

        int visitCount = visitRepository.recordVisits(request.visitorId(), request.visitedId());

        actionEventPublisher.publishVisit(request.visitorId());
        LOGGER.debug("Visit recorded: visitor={} visited={} count={}",
                request.visitorId(), request.visitedId(), visitCount);

        return new VisitResponse(request.visitorId(), request.visitedId(), visitCount, Instant.now()
        );
    }

    @Transactional
    public LikeResponse recordLike(LikeRequest request) {
        checkIfNotBlocked(request.likerId());

        boolean isNew = likeRepository.recordLikes(request.likerId(), request.likedId());

        actionEventPublisher.publishLike(request.likerId());

        LOGGER.debug("Like recorded: liker={} liked={} isNew={}",
                request.likerId(), request.likedId(), isNew);

        return new LikeResponse(
                request.likerId(),
                request.likedId(),
                isNew,
                Instant.now()
        );
    }

    @Transactional(readOnly = true)
    public List<VisitorSummary> getVisitors(UUID userId, int page, int size) {
        int offset = page * size;
        return visitRepository.findVisitors(userId, size, offset);
    }

    private void checkIfNotBlocked(UUID userId) {
        if (blockUserRespository.isBlocked(userId)) {
            throw new UserBlockedException(userId);
        }
    }
}
