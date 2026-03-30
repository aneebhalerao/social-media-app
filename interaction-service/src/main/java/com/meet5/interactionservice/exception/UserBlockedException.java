package com.meet5.interactionservice.exception;

import java.util.UUID;

public class UserBlockedException extends RuntimeException {
    public UserBlockedException(UUID userId) {
        super("User " +userId+ "is blocked and detected as fraud");
    }
}
