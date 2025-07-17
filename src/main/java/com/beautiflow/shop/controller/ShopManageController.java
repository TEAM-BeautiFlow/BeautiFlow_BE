package com.beautiflow.shop.controller;

import com.beautiflow.global.common.ApiResponse;
import com.beautiflow.global.domain.TreatmentCategory;
import com.beautiflow.shop.dto.ShopInfoRes;
import com.beautiflow.shop.dto.ShopUpdateReq;
import com.beautiflow.treatment.dto.TreatmentInfoRes;
import com.beautiflow.shop.service.ShopManageService;
import com.beautiflow.treatment.dto.TreatmentUpdateReq;
import com.beautiflow.treatment.service.TreatmentService;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/shops/manage")
public class ShopManageController {

  private final ShopManageService shopManageService;
  private final TreatmentService treatmentService;

  @GetMapping("/{shopId}")
  public ResponseEntity<ApiResponse<ShopInfoRes>> getShopDetails(@PathVariable Long shopId) {
    ShopInfoRes shopDetails = shopManageService.getShopDetails(shopId);

    return ResponseEntity.ok(ApiResponse.success(shopDetails));
  }

  @PatchMapping("/{shopId}")
  public ResponseEntity<ApiResponse<ShopInfoRes>> updateShopDetails(
      @PathVariable Long shopId,
      @ModelAttribute ShopUpdateReq requestDto,
      @RequestParam(value = "newImages", required = false) List<MultipartFile> newImages
  ) {
    ShopInfoRes updatedShop = shopManageService.updateShopDetailsAndImages(shopId, requestDto, newImages);
    return ResponseEntity.ok(ApiResponse.success(updatedShop));
  }

  // 매장 시술 목록 조회
  @GetMapping("/{shopId}/treatments")
  public ResponseEntity<ApiResponse<List<TreatmentInfoRes>>> getTreatments(
      @PathVariable Long shopId,
      @Parameter(description = "시술 카테고리 (hand, feet, cf)")
      @RequestParam TreatmentCategory category
  ) {
    List<TreatmentInfoRes> treatments = treatmentService.getTreatments(shopId, category);
    return ResponseEntity.ok(ApiResponse.success(treatments));
  }

  // 매장 시술 정보 수정
  @PatchMapping(value = "/treatments/{treatmentId}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
  public ResponseEntity<ApiResponse<TreatmentInfoRes>> updateTreatment(
      @PathVariable Long treatmentId,
      @RequestPart("requestDto") TreatmentUpdateReq requestDto,
      @RequestPart(value = "newImages", required = false) List<MultipartFile> newImages
  ) {
    TreatmentInfoRes updatedTreatment = treatmentService.updateTreatment(treatmentId, requestDto, newImages);
    return ResponseEntity.ok(ApiResponse.success(updatedTreatment));
  }
}