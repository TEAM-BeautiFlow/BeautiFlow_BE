package com.beautiflow.user.controller;

import com.beautiflow.global.common.security.authentication.CustomOAuth2User;
import com.beautiflow.global.common.ApiResponse;
import com.beautiflow.user.dto.SignUpReq;
import com.beautiflow.user.dto.SignUpRes;
import com.beautiflow.user.dto.TokenReq;
import com.beautiflow.user.dto.TokenRes;
import com.beautiflow.user.service.RefreshService;
import com.beautiflow.user.dto.UserStylePatchReq;
import com.beautiflow.user.dto.UserStyleReq;
import com.beautiflow.user.dto.UserStyleRes;
import com.beautiflow.user.service.SignUpService;
import com.beautiflow.user.service.UserStyleService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final SignUpService signUpService;
    private final RefreshService refreshService;
    private final UserStyleService userStyleService;


    @PostMapping("/signup")
    public ResponseEntity<SignUpRes> signUp(@RequestBody SignUpReq signUpReq) {
        SignUpRes signUpRes = signUpService.signUp(signUpReq);
        return ResponseEntity.ok(signUpRes);
    }


    @PostMapping("/refresh")
    public ResponseEntity<TokenRes> refresh(@RequestBody TokenReq tokenReq) {
        TokenRes tokenRes = refreshService.reissue(tokenReq);
        return ResponseEntity.ok(tokenRes);
    }



    @PostMapping(value= "/style", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserStyleRes>> postStyle(
            @AuthenticationPrincipal CustomOAuth2User user,
            @RequestPart("request") UserStyleReq userStyleReq,
            @RequestPart("images") List<MultipartFile> images) {
        Long userId =user.getUserId();
        UserStyleRes userStyleRes = userStyleService.postUserStyle(userId,userStyleReq, images);
        return ResponseEntity.ok((ApiResponse.success(userStyleRes)));
    }

    @GetMapping("/style")
    public ResponseEntity<ApiResponse<UserStyleRes>> getStyle(@AuthenticationPrincipal CustomOAuth2User user) {
        UserStyleRes res = userStyleService.getUserStyle(user.getUserId());
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    @PatchMapping(value = "/style", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserStyleRes>> patchStyle(
            @AuthenticationPrincipal CustomOAuth2User user,
            @RequestPart("request")UserStylePatchReq userStylePatchReq,
            @RequestPart("newImages") List<MultipartFile> newImages
    ) {
        UserStyleRes res = userStyleService.patchUserStyle(user.getUserId(), userStylePatchReq, newImages);
        return ResponseEntity.ok(ApiResponse.success(res));
    }



}
