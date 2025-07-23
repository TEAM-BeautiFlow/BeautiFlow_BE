package com.beautiflow.shop.controller;

import com.beautiflow.global.common.ApiResponse;
import com.beautiflow.shop.dto.ShopDetailRes;
import com.beautiflow.reservation.dto.response.TreatmentDetailWithOptionRes;
import com.beautiflow.reservation.dto.response.TreatmentRes;
import com.beautiflow.shop.service.ShopInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
public class ShopInfoController {

    private final ShopInfoService shopService;

    @Operation(summary = "매장 정보 조회", description = "shopId로 매장 정보 조회")
    @GetMapping("/{shopId}")
    public ResponseEntity<ApiResponse<ShopDetailRes>> getShopDetail(@PathVariable Long shopId) {
        ShopDetailRes response = shopService.getShopDetail(shopId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "매장 내 시술 목록 조회", description = "shopId로 매장 시술 목록 조회, category 필터 가능")
    @GetMapping("/{shopId}/treatments")
    public ResponseEntity<ApiResponse<List<TreatmentRes>>> getTreatmentsByShopAndCategory(
            @PathVariable Long shopId,
            @RequestParam(required = false) String category) {
        List<TreatmentRes> treatments = shopService.getTreatmentsByShopAndCategory(shopId, category);
        return ResponseEntity.ok(ApiResponse.success(treatments));
    }

    @Operation(summary = "매장 내 특정 시술 상세 조회", description = "shopId와 treatmentId로 특정 시술 상세 정보 조회")
    @GetMapping("/{shopId}/treatments/{treatmentId}")
    public ResponseEntity<ApiResponse<TreatmentRes>> getTreatmentDetail(
            @PathVariable Long shopId,
            @PathVariable Long treatmentId) {
        TreatmentRes response = shopService.getTreatmentDetail(shopId, treatmentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "매장 내 특정 시술과 시술 내 옵션 조회", description = "shopId와 treatmentId로 옵션 포함 시술 상세 정보 조회")
    @GetMapping("/{shopId}/treatments/{treatmentId}/options")
    public ResponseEntity<ApiResponse<TreatmentDetailWithOptionRes>> getTreatmentDetailWithOptions(
            @PathVariable Long shopId,
            @PathVariable Long treatmentId) {
        TreatmentDetailWithOptionRes response = shopService.getTreatmentDetailWithOptions(shopId, treatmentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
