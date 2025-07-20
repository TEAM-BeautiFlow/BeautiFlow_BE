package com.beautiflow.user.controller;

import com.beautiflow.global.common.error.UserErrorCode;
import com.beautiflow.global.common.exception.BeautiFlowException;
import com.beautiflow.global.common.security.CustomOAuth2User;
import com.beautiflow.global.common.util.JWTUtil;
import com.beautiflow.user.dto.SignUpReq;
import com.beautiflow.user.dto.SignUpRes;
import com.beautiflow.user.dto.TokenReq;
import com.beautiflow.user.dto.TokenRes;
import com.beautiflow.user.service.SignUpService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final JWTUtil jwtUtil;

    private final SignUpService signUpService;

    @PostMapping("/signup")
    public ResponseEntity<SignUpRes> signUp(@RequestBody SignUpReq signUpReq) {
        SignUpRes signUpRes = signUpService.signUp(signUpReq);
        return ResponseEntity.ok(signUpRes);
    }

    @PostMapping("/example")
    public ResponseEntity<String> example(
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User
    ) {
        Long userId = customOAuth2User.getUserId();
        String name = customOAuth2User.getName();

        String message = "✅ 인증 성공! userId: " + userId + ", name: " + name;
        return ResponseEntity.ok(message);
    }


    @PostMapping("/refresh")
    public ResponseEntity<TokenRes> refresh(@RequestBody TokenReq tokenReq) {
        String refreshToken = tokenReq.refreshToken();
        String accessToken =  tokenReq.accessToken();

        Long userId = jwtUtil.getUserId(refreshToken);
        String email = "refresh:" + userId;

        Claims claims = jwtUtil.checkRefreshToken(refreshToken, email)
                .orElseThrow(() -> new BeautiFlowException(UserErrorCode.TOKEN_GENERATION_FAILED));

        boolean isRefreshVaild;
        try {
            isRefreshVaild = jwtUtil.validateToken(refreshToken);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            isRefreshVaild = false;
        } catch (Exception e) {
            throw new BeautiFlowException(UserErrorCode.JWT_TOKEN_INVALID);
        }

        if (!isRefreshVaild) {
            throw new BeautiFlowException(UserErrorCode.JWT_TOKEN_INVALID);
        }


        boolean accessStillValid;
        try {
            accessStillValid = jwtUtil.validateToken(accessToken);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            accessStillValid = false; // 만료되었으면 false 처리
        } catch (Exception e) {
            throw new BeautiFlowException(UserErrorCode.JWT_TOKEN_INVALID);
        }

        if (accessStillValid) {
            throw new BeautiFlowException(UserErrorCode.ACCESS_TOKEN_STILL_VALID);
        }




        String kakaoId = claims.get("kakaoId", String.class);
        String provider = claims.get("provider", String.class);

        String newAccessToken = jwtUtil.createAccessToken(provider, kakaoId, userId);

        return ResponseEntity.ok(new TokenRes(newAccessToken));
    }


}
