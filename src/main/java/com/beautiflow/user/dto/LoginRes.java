package com.beautiflow.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record LoginRes(
        String kakaoId,
        String provider,
        String isNewUser,
        String email,
        String accessToken,
        String refreshToken

) {
}