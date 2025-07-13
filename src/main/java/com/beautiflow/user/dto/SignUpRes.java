package com.beautiflow.user.dto;

import lombok.Builder;

@Builder
public record SignUpRes(
        Long id,
        String kakaoId,
        String provider,
        String name,
        String contact,
        String accessToken,
        String refreshToken
) {

}
