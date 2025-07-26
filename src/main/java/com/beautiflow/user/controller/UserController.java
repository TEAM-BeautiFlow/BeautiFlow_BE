package com.beautiflow.user.controller;

import com.beautiflow.global.common.ApiResponse;
import com.beautiflow.global.common.security.authentication.CustomOAuth2User;
import com.beautiflow.user.dto.SignUpReq;
import com.beautiflow.user.dto.SignUpRes;
import com.beautiflow.user.dto.TokenReq;
import com.beautiflow.user.dto.TokenRes;
import com.beautiflow.user.service.UserExitService;
import com.beautiflow.user.service.RefreshService;
import com.beautiflow.user.service.SignUpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final SignUpService signUpService;
    private final RefreshService refreshService;
    private final UserExitService logoutService;

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

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>>logout(@AuthenticationPrincipal CustomOAuth2User user) {
        long userId = user.getUserId();
        logoutService.logout(userId);
        return ResponseEntity.ok(ApiResponse.successWithNoData());
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<Void>> delete(@AuthenticationPrincipal CustomOAuth2User user) {
        long userId = user.getUserId();
        logoutService.delete(userId);
        return ResponseEntity.ok(ApiResponse.successWithNoData());
    }



}
