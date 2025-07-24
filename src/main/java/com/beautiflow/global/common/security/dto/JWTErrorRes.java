package com.beautiflow.global.common.security.dto;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record JWTErrorRes(
        LocalDateTime timestamp,
        int status,
        String error,
        String path
) {
}
