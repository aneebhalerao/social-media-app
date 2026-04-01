package com.meet5.userservice.service;

import com.meet5.userservice.domain.User;
import com.meet5.userservice.dto.BulkInsertResponse;
import com.meet5.userservice.dto.UserRequest;
import com.meet5.userservice.dto.UserResponse;
import com.meet5.userservice.exception.DuplicateUsernameException;
import com.meet5.userservice.exception.UserNotFoundException;
import com.meet5.userservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {
    Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserResponse createUser(UserRequest userRequest) {
        userRepository.findByUsername(userRequest.username()).ifPresent(existingUser -> {
            throw new DuplicateUsernameException(userRequest.username());
        });

        User user = mapToUser(userRequest);
        User savedUser = userRepository.insert(user);
        LOGGER.debug("User created: {}", savedUser.toString());
        return mapToUserResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
       return userRepository.findById(id)
               .map(this::mapToUserResponse)
               .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Transactional
    public BulkInsertResponse bulkCreateUsers(List<UserRequest> requests) {
        long startMs = System.currentTimeMillis();
        List<User> users = requests.stream()
                .map(req -> User.builder()
                        .name(req.name())
                        .username(req.username())
                        .age(req.age())
                        .extraFields(req.extraFields() != null
                                ? req.extraFields()
                                : Collections.emptyMap())
                        .build())
                .toList();
        int inserted = userRepository.bulkInsert(users);
        int skipped  = requests.size() - inserted;
        long duration = System.currentTimeMillis() - startMs;

        LOGGER.info("Bulk insert complete: total={} inserted={} skipped={} durationMs={}", requests.size(), inserted, skipped, duration);

        return new BulkInsertResponse(inserted, skipped, requests.size(), duration);
    }

    private User mapToUser(UserRequest request) {
        return User.builder()
                .name(request.name())
                .username(request.username())
                .age(request.age())
                .extraFields(request.extraFields() != null ? request.extraFields() : Collections.emptyMap())
                .build();
    }

    private UserResponse mapToUserResponse(User user) {
        return new UserResponse(user.getId(),
                user.getName(),
                user.getUsername(),
                user.getAge(),
                user.getStatus(),
                user.getExtraFields(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }
}
