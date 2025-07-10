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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Reservation", description = "고객_매장/예약")
@RestController
@RequestMapping("/shops")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;

    @Operation(summary = "매장 정보 조회", description = "shopId로 매장 정보 조회")
    @GetMapping("/{shopId}")
    public ResponseEntity<ApiResponse<ShopDetailResponse>> getShopDetail(@PathVariable Long shopId) {
        ShopDetailResponse response = shopService.getShopDetail(shopId);
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
}
