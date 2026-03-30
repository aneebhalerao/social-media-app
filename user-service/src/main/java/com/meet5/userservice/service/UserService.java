package com.meet5.userservice.service;

import com.meet5.userservice.domain.User;
import com.meet5.userservice.dto.UserRequest;
import com.meet5.userservice.dto.UserResponse;
import com.meet5.userservice.exception.DuplicateUsernameException;
import com.meet5.userservice.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.UUID;

@Service
public class UserService {
    Logger logger = LoggerFactory.getLogger(UserService.class);

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
        logger.info("User created: " + savedUser.toString());
        return mapToUserResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
       return userRepository.findById(id)
               .map(this::mapToUserResponse)
               .orElseThrow(RuntimeException::new);
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
