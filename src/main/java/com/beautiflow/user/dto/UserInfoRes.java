package com.beautiflow.user.dto;

import lombok.Builder;

@Builder
public record UserInfoRes(
        Long id,
        String kakaoId,
        String name,
        String contact,
        String email,
        Long shopId
) {

}
