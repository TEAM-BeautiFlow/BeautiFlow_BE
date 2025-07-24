package com.beautiflow.user.controller;

import com.beautiflow.global.common.error.UserErrorCode;
import com.beautiflow.global.common.exception.BeautiFlowException;
import com.beautiflow.global.common.security.authentication.CustomOAuth2User;
import com.beautiflow.global.common.util.JWTUtil;
import com.beautiflow.user.dto.SignUpReq;
import com.beautiflow.user.dto.SignUpRes;
import com.beautiflow.user.dto.TokenReq;
import com.beautiflow.user.dto.TokenRes;
import com.beautiflow.user.service.RefreshService;
import com.beautiflow.user.service.SignUpService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final JWTUtil jwtUtil;

    private final SignUpService signUpService;
    private final RefreshService refreshService;

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
        TokenRes tokenRes = refreshService.reissue(tokenReq);
        return ResponseEntity.ok(tokenRes);
    }


}
