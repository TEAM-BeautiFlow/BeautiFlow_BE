package com.beautiflow.reservation.controller;

import com.beautiflow.global.common.ApiResponse;
import com.beautiflow.global.common.error.ErrorCode;
import com.beautiflow.global.common.error.ShopErrorCode;
import com.beautiflow.global.common.error.TreatmentErrorCode;
import com.beautiflow.global.common.success.CommonSuccessCode;
import com.beautiflow.global.common.success.SuccessCode;
import com.beautiflow.reservation.dto.response.ShopDetailResponse;
import com.beautiflow.reservation.dto.response.TreatmentResponse;
import com.beautiflow.reservation.service.ShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name="Reservation", description = "고객_매장/예약")
@RestController
@RequestMapping("/shops")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;

    @Operation(summary = "매장 정보 조회", description = "shopId로 매장 정보 조회")
    @GetMapping("/{shopId}")
    public ApiResponse<?> getShopDetail(@PathVariable Long shopId) {
        try {
            ShopDetailResponse response = shopService.getShopDetail(shopId);
            return ApiResponse.success(response);
        } catch (EntityNotFoundException e) {
            return ApiResponse.createFail(ShopErrorCode.SHOP_NOT_FOUND, e.getMessage());
        }
    }

    @Operation(summary = "매장 내 시술 목록 조회", description = "shopId로 매장 시술 목록 조회, category 필터 가능")
    @GetMapping("/{shopId}/treatments")
    public ApiResponse<List<TreatmentResponse>> getTreatmentsByShopAndCategory(
            @PathVariable Long shopId,
            @RequestParam(required = false) String category) {
        try {
            List<TreatmentResponse> treatments = shopService.getTreatmentsByShopAndCategory(shopId, category);
            return ApiResponse.success(treatments);
        } catch (EntityNotFoundException e) {
            return (ApiResponse<List<TreatmentResponse>>) ApiResponse.createFail(ShopErrorCode.SHOP_NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            return (ApiResponse<List<TreatmentResponse>>) ApiResponse.createFail(TreatmentErrorCode.INVALID_TREATMENT_PARAMETER, e.getMessage());
        }
    }

    @Operation(summary = "매장 내 특정 시술 상세 조회", description = "shopId와 treatmentId로 특정 시술 상세 정보 조회")
    @GetMapping("/{shopId}/treatments/{treatmentId}")
    public ApiResponse<TreatmentResponse> getTreatmentDetail(
            @PathVariable Long shopId,
            @PathVariable Long treatmentId) {
        try {
            TreatmentResponse response = shopService.getTreatmentDetail(shopId, treatmentId);
            return ApiResponse.success(response);
        } catch (EntityNotFoundException e) {
            if (e.getMessage().contains("시술")) {
                return (ApiResponse<TreatmentResponse>) ApiResponse.createFail(TreatmentErrorCode.TREATMENT_NOT_FOUND, e.getMessage());
            } else {
                return (ApiResponse<TreatmentResponse>) ApiResponse.createFail(ShopErrorCode.SHOP_NOT_FOUND, e.getMessage());
            }
        }
    }
}
