package com.beautiflow.user.controller;

import com.beautiflow.global.common.ApiResponse;
import com.beautiflow.global.common.ApiResponse;
import com.beautiflow.global.common.error.UserErrorCode;
import com.beautiflow.global.common.exception.BeautiFlowException;
import com.beautiflow.global.common.security.authentication.CustomOAuth2User;
import com.beautiflow.global.common.ApiResponse;
import com.beautiflow.user.dto.LoginReq;
import com.beautiflow.user.dto.LoginRes;
import com.beautiflow.user.dto.SignUpReq;
import com.beautiflow.user.dto.SignUpRes;
import com.beautiflow.user.dto.TokenReq;
import com.beautiflow.user.dto.TokenRes;
import com.beautiflow.user.service.LoginService;
import com.beautiflow.user.service.UserExitService;
import com.beautiflow.user.dto.UserInfoReq;
import com.beautiflow.user.dto.UserInfoRes;
import com.beautiflow.user.service.MyPageService;
import com.beautiflow.user.service.RefreshService;
import com.beautiflow.user.dto.UserStylePatchReq;
import com.beautiflow.user.dto.UserStyleReq;
import com.beautiflow.user.dto.UserStyleRes;
import com.beautiflow.user.service.SignUpService;
import com.beautiflow.user.service.UserStyleService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
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
    private final MyPageService myPageService;
    private final UserExitService userExitService;
    private final UserStyleService userStyleService;
    private final LoginService loginService;

    @PostMapping("/login")
    @Operation(summary = "로그인 API",
            description = "Response로 카카오에서 받아온 유저 기본 정보를 반환합니다" )
    public ResponseEntity<ApiResponse<LoginRes>> login(@RequestBody LoginReq loginReq){
        LoginRes loginRes = loginService.login(loginReq);
        return ResponseEntity.ok(ApiResponse.success(loginRes));
    }


    @PostMapping("/signup")
    @Operation(summary = "회원가입 API",
            description = "신규 사용자의 경우 회원가입 후 토큰을 발행합니다" )
    public ResponseEntity<ApiResponse<SignUpRes>> signUp(@RequestBody SignUpReq signUpReq) {
        SignUpRes signUpRes = signUpService.signUp(signUpReq);
        return ResponseEntity.ok(ApiResponse.success(signUpRes));
    }


    @PostMapping("/refresh")
    @Operation(summary = "회원가입 API",
            description = "신규 사용자의 경우 회원가입 후 토큰을 발행합니다" )
    public ResponseEntity<ApiResponse<TokenRes>> refresh(@RequestBody TokenReq tokenReq) {
        TokenRes tokenRes = refreshService.reissue(tokenReq);
        return ResponseEntity.ok(ApiResponse.success(tokenRes));
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃 API",
            description = "Redis에서 refresh token을 삭제합니다. access token은 클라이언트에서 삭제합니다" )
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal CustomOAuth2User user) {
        long userId = user.getUserId();
        userExitService.logout(userId);
        return ResponseEntity.ok(ApiResponse.successWithNoData());
    }



    @DeleteMapping("/delete")
    @Operation(summary = "회원 탈퇴 API",
            description = "soft delete로 회원 탈퇴를 구현한 api입니다." )
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal CustomOAuth2User user) {
        long userId = user.getUserId();
        userExitService.delete(userId);
        return ResponseEntity.ok(ApiResponse.successWithNoData());
    }



    @PostMapping(value = "/style", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "사용자 선호 스타일 등록 API", description="content-type으로 multipart/form-data 사용합니다.")
    public ResponseEntity<ApiResponse<UserStyleRes>> postStyle(
            @AuthenticationPrincipal CustomOAuth2User user,
            @RequestPart("request") UserStyleReq userStyleReq,
            @RequestPart(value="images", required = false) List<MultipartFile> images) {
        Long userId =user.getUserId();
        UserStyleRes userStyleRes = userStyleService.postUserStyle(userId,userStyleReq, images);
        return ResponseEntity.ok((ApiResponse.success(userStyleRes)));
    }

    @GetMapping("/style")
    @Operation(summary = "사용자 선호 스타일 조회 API")
    public ResponseEntity<ApiResponse<UserStyleRes>> getStyle(
            @AuthenticationPrincipal CustomOAuth2User user) {
        UserStyleRes res = userStyleService.getUserStyle(user.getUserId());
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    @PatchMapping(value = "/style", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "사용자 선호 스타일 수정 API", description="content-type으로 multipart/form-data 사용합니다.")
    public ResponseEntity<ApiResponse<UserStyleRes>> patchStyle(
            @AuthenticationPrincipal CustomOAuth2User user,
            @RequestPart("request") UserStylePatchReq userStylePatchReq,
            @RequestPart(value = "newImages", required = false) List<MultipartFile> newImages

    ) {
        UserStyleRes res = userStyleService.patchUserStyle(user.getUserId(), userStylePatchReq,
                newImages);
        return ResponseEntity.ok(ApiResponse.success(res));
    }


    @PatchMapping("/info")
    @Operation(summary = "내 정보 수정 API")
    public ResponseEntity<ApiResponse<UserInfoRes>> patchInfo(
            @AuthenticationPrincipal CustomOAuth2User user, @RequestBody UserInfoReq userInfoReq) {
        Long userId = user.getUserId();
        UserInfoRes userInfoRes = myPageService.patchUserInfo(userId, userInfoReq);
        return ResponseEntity.ok(ApiResponse.success(userInfoRes));
    }

    @GetMapping("/info")
    @Operation(summary = "내 정보 조회 API")
    public ResponseEntity<ApiResponse<UserInfoRes>> getInfo(
            @AuthenticationPrincipal CustomOAuth2User user) {
        Long userId = user.getUserId();
        UserInfoRes userInfoRes = myPageService.getUserInfo(userId);
        return ResponseEntity.ok(ApiResponse.success(userInfoRes));
    }


}
