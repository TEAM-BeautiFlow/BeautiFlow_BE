package com.beautiflow.user.dto;

import lombok.Builder;

@Builder
public record SignUpRes(
        Long id,
        String kakaoId,
        String provider,
        String name,
        String contact,
        String email,
        boolean deleted,
        String accessToken,
        String refreshToken
) {

}
