package com.beautiflow.shop.controller;

import com.beautiflow.global.common.ApiResponse;
import com.beautiflow.global.common.security.CustomOAuth2User;
import com.beautiflow.shop.dto.ShopApplyRes;
import com.beautiflow.shop.dto.ShopMemberInfoReq;
import com.beautiflow.shop.dto.ShopMemberInfoRes;
import com.beautiflow.shop.service.ShopMemberService;
import com.beautiflow.user.dto.UserStylePatchReq;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/shopmembers")
@RequiredArgsConstructor
public class ShopMemberController {

    private final ShopMemberService shopMemberService;

    @Operation(summary = "디자이너 정보 수정")
    @PatchMapping(value="/{shopId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ShopMemberInfoRes>>patchInfo (
            @PathVariable Long shopId,
            @AuthenticationPrincipal CustomOAuth2User currentUser,
            @RequestPart("request") ShopMemberInfoReq shopMemberInfoReq,
            @RequestPart("image") MultipartFile image) {
        Long userId = currentUser.getUserId();
        ShopMemberInfoRes shopMemberInfoRes = shopMemberService.patchInfo(shopId, userId, shopMemberInfoReq, image);
        return ResponseEntity.ok(ApiResponse.success(shopMemberInfoRes));
    }

}
