package com.beautiflow.shop.controller;

import com.beautiflow.global.common.ApiResponse;
import com.beautiflow.shop.dto.ShopApplyRes;
import com.beautiflow.shop.dto.ShopDetailRes;
import com.beautiflow.global.common.security.authentication.CustomOAuth2User;
import com.beautiflow.reservation.dto.response.TreatmentDetailWithOptionResponse;
import com.beautiflow.reservation.dto.response.TreatmentResponse;
import com.beautiflow.shop.dto.ShopExistsRes;
import com.beautiflow.shop.service.ShopService;
import com.beautiflow.shop.dto.ShopRegistrationReq;
import com.beautiflow.shop.dto.ShopRegistrationRes;
import com.beautiflow.shop.service.ShopOnboardingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Reservation", description = "고객_매장/예약")
@RestController
@RequestMapping("/shops")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;
    private final ShopOnboardingService shopOnboardingService;



    @Operation(summary = "매장 정보 조회", description = "shopId로 매장 정보 조회")
    @GetMapping("/{shopId}")
    public ResponseEntity<ApiResponse<ShopDetailRes>> getShopDetail(@PathVariable Long shopId) {
        ShopDetailRes response = shopService.getShopDetail(shopId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "매장 내 시술 목록 조회", description = "shopId로 매장 시술 목록 조회, category 필터 가능")
    @GetMapping("/{shopId}/treatments")
    public ResponseEntity<ApiResponse<List<TreatmentResponse>>> getTreatmentsByShopAndCategory(
            @PathVariable Long shopId,
            @RequestParam(required = false) String category) {
        List<TreatmentResponse> treatments = shopService.getTreatmentsByShopAndCategory(shopId, category);
        return ResponseEntity.ok(ApiResponse.success(treatments));
    }

    @Operation(summary = "매장 내 특정 시술 상세 조회", description = "shopId와 treatmentId로 특정 시술 상세 정보 조회")
    @GetMapping("/{shopId}/treatments/{treatmentId}")
    public ResponseEntity<ApiResponse<TreatmentResponse>> getTreatmentDetail(
            @PathVariable Long shopId,
            @PathVariable Long treatmentId) {
        TreatmentResponse response = shopService.getTreatmentDetail(shopId, treatmentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "매장 내 특정 시술과 시술 내 옵션 조회", description = "shopId와 treatmentId로 옵션 포함 시술 상세 정보 조회")
    @GetMapping("/{shopId}/treatments/{treatmentId}/options")
    public ResponseEntity<ApiResponse<TreatmentDetailWithOptionResponse>> getTreatmentDetailWithOptions(
            @PathVariable Long shopId,
            @PathVariable Long treatmentId) {
        TreatmentDetailWithOptionResponse response = shopService.getTreatmentDetailWithOptions(shopId, treatmentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "샵 등록", description = "새로운 샵을 등록하는 API입니다.")
    @PostMapping
    public ResponseEntity<ShopRegistrationRes> register(
            @RequestBody ShopRegistrationReq shopRegistrationReq,
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
        Long userId = customOAuth2User.getUserId();
        ShopRegistrationRes shopRegistrationRes = shopOnboardingService.registerShop(userId, shopRegistrationReq);
        return ResponseEntity.ok(shopRegistrationRes);

    }

    @Operation(summary = "입사 신청", description = "직원이 샵에 입사를 신청하는 API입니다.")
    @PostMapping("/{shopId}/apply")
    public ResponseEntity<ShopApplyRes> applyToShop(@PathVariable Long shopId,
            @AuthenticationPrincipal CustomOAuth2User currentUser) {
        Long userId = currentUser.getUserId();
        ShopApplyRes shopApplyRes = shopOnboardingService.ApplyToShop(userId, shopId);
        return ResponseEntity.ok(shopApplyRes);
    }

    @GetMapping(value = "/exists", params = "businessNumber")
    public ResponseEntity<ApiResponse<ShopExistsRes>>exists(
            @AuthenticationPrincipal CustomOAuth2User currentUser
            ,@RequestParam String businessNumber) {
        Long userId = currentUser.getUserId();
        ShopExistsRes shopExistsRes = shopOnboardingService.IsShopExists(userId, businessNumber);
        return ResponseEntity.ok(ApiResponse.success(shopExistsRes));
    }
}
