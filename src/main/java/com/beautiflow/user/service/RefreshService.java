package com.beautiflow.user.service;

import com.beautiflow.global.common.error.UserErrorCode;
import com.beautiflow.global.common.exception.BeautiFlowException;
import com.beautiflow.global.common.util.JWTUtil;
import com.beautiflow.user.dto.TokenReq;
import com.beautiflow.user.dto.TokenRes;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshService {

    private final JWTUtil jwtUtil;

    public TokenRes reissue(TokenReq tokenReq) {
        String refreshToken = tokenReq.refreshToken();
        String accessToken = tokenReq.accessToken();

        Long userId = jwtUtil.getUserId(refreshToken);
        String email = "refresh:" + userId;

        Claims claims = jwtUtil.checkRefreshToken(refreshToken, email)
                .orElseThrow(() -> new BeautiFlowException(UserErrorCode.TOKEN_GENERATION_FAILED));

        boolean isRefreshValid;
        try {
            isRefreshValid = jwtUtil.validateToken(refreshToken);
        } catch (ExpiredJwtException e) {
            isRefreshValid = false;
        } catch (Exception e) {
            throw new BeautiFlowException(UserErrorCode.JWT_TOKEN_INVALID);
        }

        if (!isRefreshValid) {
            throw new BeautiFlowException(UserErrorCode.JWT_TOKEN_INVALID);
        }

        boolean accessStillValid;
        try {
            accessStillValid = jwtUtil.validateToken(accessToken);
        } catch (ExpiredJwtException e) {
            accessStillValid = false;
        } catch (Exception e) {
            throw new BeautiFlowException(UserErrorCode.JWT_TOKEN_INVALID);
        }

        if (accessStillValid) {
            throw new BeautiFlowException(UserErrorCode.ACCESS_TOKEN_STILL_VALID);
        }

        String kakaoId = claims.get("kakaoId", String.class);
        String provider = claims.get("provider", String.class);

        String newAccessToken = jwtUtil.createAccessToken(provider, kakaoId, userId);

        return new TokenRes(newAccessToken);
    }
}