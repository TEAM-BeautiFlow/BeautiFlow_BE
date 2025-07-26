package com.beautiflow.global.common.security.dto;

import lombok.Builder;

@Builder
public record JWTErrorRes(
        boolean success,
        int code,
        String message,
        String data
) {
}
