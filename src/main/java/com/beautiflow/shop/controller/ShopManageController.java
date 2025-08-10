package com.beautiflow.shop.controller;

import com.beautiflow.global.common.ApiResponse;
import com.beautiflow.shop.dto.BusinessHourUpdateReq;
import com.beautiflow.shop.dto.ShopInfoRes;
import com.beautiflow.shop.dto.ShopUpdateReq;
import com.beautiflow.shop.service.ShopManageService;
import com.beautiflow.treatment.dto.TreatmentUpdateReq;
import com.beautiflow.treatment.service.TreatmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

  @Operation(summary = "매장 상세 정보 조회", description = "shopId로 매장 상세 정보 조회")
  @GetMapping("/{shopId}")
  public ResponseEntity<ApiResponse<ShopInfoRes>> getShopDetails(@PathVariable Long shopId) {
    ShopInfoRes shopDetails = shopManageService.getShopDetails(shopId);

    return ResponseEntity.ok(ApiResponse.success(shopDetails));
  }

  @Operation(summary = "매장 정보 및 이미지 수정", description = "shopId로 매장 상세 정보/이미지 수정, 수정할 부분만 body에 포함시키면 됨")
  @PatchMapping("/{shopId}")
  public ResponseEntity<ApiResponse<ShopInfoRes>> updateShopDetails(
      @PathVariable Long shopId,
      @ModelAttribute ShopUpdateReq requestDto,
      @RequestParam(value = "newImages", required = false) List<MultipartFile> newImages
  ) {
    ShopInfoRes updatedShop = shopManageService.updateShopDetailsAndImages(shopId, requestDto, newImages);
    return ResponseEntity.ok(ApiResponse.success(updatedShop));
  }

  @Operation(summary = "매장 영업시간 일괄 수정", description = "해당 매장의 일주일치 영업시간 전체를 새로 설정합니다.")
  @PutMapping("/shops/{shopId}/business-hours")
  public ResponseEntity<ApiResponse<Void>> updateBusinessHours(
      @Parameter(description = "매장 ID", required = true)
      @PathVariable Long shopId,

      @Valid @RequestBody BusinessHourUpdateReq requestDto // @Valid로 유효성 검증
  ) {
    shopManageService.updateBusinessHours(shopId, requestDto);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @Operation(summary = "매장 내 특정 시술 정보 수정", description = "shopId와 treatmentId로 시술 정보(옵션 포함)를 수정합니다.")
  @PatchMapping("/{shopId}/treatments/{treatmentId}")
  public ResponseEntity<ApiResponse<Void>> updateTreatment(
      @Parameter(description = "매장 ID", required = true)
      @PathVariable("shopId") Long shopId,

      @Parameter(description = "시술 ID", required = true)
      @PathVariable("treatmentId") Long treatmentId,

      @Parameter(description = "시술 및 옵션 정보(JSON)")
      @Valid @RequestPart("requestDto") TreatmentUpdateReq requestDto,

      @Parameter(description = "새로 추가할 이미지 파일 목록")
      @RequestPart(value = "newImages", required = false) List<MultipartFile> newImages,

      @Parameter(description = "삭제할 기존 이미지 ID 목록")
      @RequestParam(value = "deleteImageIds", required = false) List<Long> deleteImageIds
  ) {
    treatmentService.updateTreatment(treatmentId, requestDto, newImages, deleteImageIds);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @Operation(summary = "매장 사업자 등록증 이미지 제출", description = "shopId로 사업자 등록증 제출")
  @PostMapping(value = "/{shopId}/license-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse<String>> uploadLicenseImage(
      @PathVariable Long shopId,
      @RequestParam(value = "licenseImage") MultipartFile licenseImage
  ){
    String licenseImageUrl = shopManageService.uploadLicenseImage(shopId, licenseImage);
    return ResponseEntity.ok(ApiResponse.success(licenseImageUrl));
  }

  @Operation(summary = "매장 사업자 등록증 이미지 조회", description = "shopId로 제출한 사업자 등록증 조회")
  @GetMapping("/{shopId}/license-image")
  public ResponseEntity<ApiResponse<String>> getLicenseImage(@PathVariable Long shopId) {
    String licenseImageUrl = shopManageService.getLicenseImageUrl(shopId);
    return ResponseEntity.ok(ApiResponse.success(licenseImageUrl));
  }

}