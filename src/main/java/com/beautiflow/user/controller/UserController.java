package com.beautiflow.user.controller;

import com.beautiflow.global.common.ApiResponse;
import com.beautiflow.global.common.error.UserErrorCode;
import com.beautiflow.global.common.exception.BeautiFlowException;
import com.beautiflow.global.common.security.authentication.CustomOAuth2User;
import com.beautiflow.global.common.util.JWTUtil;
import com.beautiflow.user.dto.SignUpReq;
import com.beautiflow.user.dto.SignUpRes;
import com.beautiflow.user.dto.TokenReq;
import com.beautiflow.user.dto.TokenRes;
import com.beautiflow.user.dto.UserInfoReq;
import com.beautiflow.user.dto.UserInfoRes;
import com.beautiflow.user.service.MyPageService;
import com.beautiflow.user.service.RefreshService;
import com.beautiflow.user.service.SignUpService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final SignUpService signUpService;
    private final RefreshService refreshService;
    private final MyPageService  myPageService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignUpRes>> signUp(@RequestBody SignUpReq signUpReq) {
        SignUpRes signUpRes = signUpService.signUp(signUpReq);
        return ResponseEntity.ok(ApiResponse.success(signUpRes));
    }


    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenRes>> refresh(@RequestBody TokenReq tokenReq) {
        TokenRes tokenRes = refreshService.reissue(tokenReq);
        return ResponseEntity.ok(ApiResponse.success(tokenRes));
    }

    @GetMapping("/info")
    public ResponseEntity<ApiResponse<UserInfoRes>> getInfo(@AuthenticationPrincipal CustomOAuth2User user) {
        Long userId = user.getUserId();
        UserInfoRes userInfoRes = myPageService.getUserInfo(userId);
        return ResponseEntity.ok(ApiResponse.success(userInfoRes));
    }

    @PatchMapping("/info")
    public ResponseEntity<ApiResponse<UserInfoRes>> patchInfo(@AuthenticationPrincipal CustomOAuth2User user, @RequestBody UserInfoReq userInfoReq) {
        Long userId = user.getUserId();
        UserInfoRes userInfoRes = myPageService.patchUserInfo(userId, userInfoReq);
        return ResponseEntity.ok(ApiResponse.success(userInfoRes));
    }






}
