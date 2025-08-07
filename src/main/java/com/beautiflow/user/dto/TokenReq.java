package com.beautiflow.user.dto;

public record TokenReq(
        String accessToken,
        String refreshToken

) {

}
