package com.beautiflow.global.common.security.dto;

import java.util.Map;
import lombok.Builder;

@Builder
public record KakaoRes(
        String provider,
        String kakaoId,
        String email
) {

    public static KakaoRes from(Map<String, Object> attributes, String provider) {
        String kakaoId = attributes.get("id").toString();
        String email = null;
        if (attributes.get("kakao_account") instanceof Map<?, ?> kakaoAccount) {
            Object emailObj = kakaoAccount.get("email");
            if (emailObj != null) {
                email = emailObj.toString();
            }
        }


        return new KakaoRes(
                provider,
                kakaoId,
                email
        );
    }


}
