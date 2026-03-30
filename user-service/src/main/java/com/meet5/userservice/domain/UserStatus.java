package com.meet5.userservice.domain;

public enum UserStatus {
    ACTIVE,
    SUSPECT,
    FRAUD,
    DELETED;

    public boolean isActionBlocked() {
        return this == FRAUD || this == DELETED;
    }
}
