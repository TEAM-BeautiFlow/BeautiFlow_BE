package com.beautiflow.user.dto;

public record SignUpReq(
        String kakaoId,
        String provider,
        String name,
        String contact,
        String email
) {

}
