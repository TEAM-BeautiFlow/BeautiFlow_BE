package com.beautiflow.user.dto;

import java.util.Map;
import lombok.Builder;

@Builder
public record KakaoRes(
        String provider,
        String kakaoId
) {

    public static KakaoRes from(Map<String, Object> attributes, String provider) {
        String kakaoId = attributes.get("id").toString();

        return new KakaoRes(
                provider,
                kakaoId
        );
    }
}
