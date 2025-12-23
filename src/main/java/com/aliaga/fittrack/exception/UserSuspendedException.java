package com.aliaga.fittrack.exception;

import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class UserSuspendedException extends RuntimeException {
    private final String reason;
    private final LocalDateTime endsAt;

    public UserSuspendedException(String reason, LocalDateTime endsAt) {
        super("Cuenta suspendida");
        this.reason = reason;
        this.endsAt = endsAt;
    }
}