package com.beautiflow.user.service;

import com.beautiflow.global.common.error.UserErrorCode;
import com.beautiflow.global.common.exception.BeautiFlowException;
import com.beautiflow.global.common.util.JWTUtil;
import com.beautiflow.user.dto.TokenReq;
import com.beautiflow.user.dto.TokenRes;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshService {

    private final JWTUtil jwtUtil;

    public TokenRes reissue(TokenReq tokenReq) {
        String refreshToken = tokenReq.refreshToken();
        String accessToken = tokenReq.accessToken();

        Claims claims = jwtUtil.parseRefresh(refreshToken);

        // access token이 아직 유효한 경우
        if (!jwtUtil.isTokenExpired(accessToken)) {
            throw new BeautiFlowException(UserErrorCode.ACCESS_TOKEN_STILL_VALID);
        }

        String kakaoId = claims.get("kakaoId", String.class);
        String provider = claims.get("provider", String.class);
        String email = claims.get("email", String.class);
        Long userId = claims.get("userId", Number.class).longValue();


        String newAccessToken = jwtUtil.createAccessToken(provider, kakaoId, userId, email);

        return new TokenRes(newAccessToken);
    }
}