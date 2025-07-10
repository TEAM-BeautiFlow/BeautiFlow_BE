package com.beautiflow.user.dto;

import lombok.Builder;

@Builder
public record LoginRes(
        String accessToken,
        String refreshToken,
        UserRes user,
        boolean isSignedUp,
        String kakaoId,
        String provider
) {

}
