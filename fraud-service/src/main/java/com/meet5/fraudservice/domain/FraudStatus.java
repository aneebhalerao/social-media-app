package com.meet5.fraudservice.domain;

public enum FraudStatus {
    CLEAN,
    SUSPECT,
    FRAUD;

    public boolean isBlocked() {
        return this == FRAUD;
    }
}
