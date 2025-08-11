package com.beautiflow.shop.controller;

import com.beautiflow.global.common.ApiResponse;
import com.beautiflow.shop.dto.BusinessHourRes;
import com.beautiflow.shop.dto.BusinessHourUpdateReq;
import com.beautiflow.shop.dto.RegularHolidayDto;
import com.beautiflow.shop.dto.ShopInfoRes;
import com.beautiflow.shop.dto.ShopUpdateReq;
import com.beautiflow.shop.dto.TreatmentUpsertReq;
import com.beautiflow.shop.dto.TreatmentUpsertRes;
import com.beautiflow.shop.service.ShopManageService;
import com.beautiflow.treatment.dto.TreatmentUpdateReq;
import com.beautiflow.treatment.service.TreatmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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

  @Operation(summary = "매장 영업시간 조회", description = "shopId로 매장 영업시간 정보 조회")
  @GetMapping("/{shopId}/business-hours")
  public ResponseEntity<ApiResponse<BusinessHourRes>> getBusinessHours(
      @Parameter(description = "매장 ID", required = true)
      @PathVariable("shopId") Long shopId
  ) {
    BusinessHourRes response = shopManageService.getBusinessHours(shopId);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @Operation(summary = "매장 영업시간 일괄 수정", description = "해당 매장의 일주일치 영업시간 전체를 새로 설정합니다.")
  @PutMapping("/{shopId}/business-hours")
  public ResponseEntity<ApiResponse<Void>> updateBusinessHours(
      @Parameter(description = "매장 ID", required = true)
      @PathVariable Long shopId,
      @Valid @RequestBody BusinessHourUpdateReq requestDto
  ) {
    shopManageService.updateBusinessHours(shopId, requestDto);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @Operation(summary = "매장 정기 휴무 일괄 수정", description = "매장의 정기 휴무 규칙을 새로 설정합니다.")
  @PutMapping("/{shopId}/holidays")
  public ResponseEntity<ApiResponse<Void>> updateRegularHolidays(
      @Parameter(description = "매장 ID", required = true) @PathVariable Long shopId,
      @Valid @RequestBody List<RegularHolidayDto> requestDtos) {
    shopManageService.updateRegularHolidays(shopId, requestDtos);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @Operation(summary = "매장 정기 휴무 조회", description = "shopId로 매장의 정기 휴무 규칙 목록을 조회합니다.")
  @GetMapping("/{shopId}/holidays")
  public ResponseEntity<ApiResponse<List<RegularHolidayDto>>> getRegularHolidays(
      @Parameter(description = "매장 ID", required = true) @PathVariable Long shopId) {
    List<RegularHolidayDto> response = shopManageService.getRegularHolidays(shopId);
    return ResponseEntity.ok(ApiResponse.success(response));
  }


  @Operation(summary = "매장 시술 생성/수정",
      description = "시술의 텍스트 정보와 옵션만 생성/수정. 이미지는 별도의 API 사용. id = null이면 신규 생성")
  @PutMapping("/{shopId}/treatments")
  public ResponseEntity<ApiResponse<List<TreatmentUpsertRes>>> upsertTreatments(
      @Parameter(description = "매장 ID", required = true)
      @PathVariable Long shopId,
      @Valid @RequestBody List<TreatmentUpsertReq> requestDtos
  ) {

    List<TreatmentUpsertRes> response = shopManageService.upsertTreatmentsWithoutImages(shopId, requestDtos);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @Operation(summary = "매장 시술 이미지 추가",
      description = "특정 시술에 새로운 이미지들을 업로드합니다. 기존 이미지는 유지됩니다.")
  @PostMapping(value = "/{shopId}/treatments/{treatmentId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse<Void>> uploadTreatmentImages(
      @Parameter(description = "매장 ID", required = true) @PathVariable Long shopId,
      @Parameter(description = "시술 ID", required = true) @PathVariable Long treatmentId,
      @Parameter(description = "업로드할 이미지 파일 목록")
      @RequestPart("images") List<MultipartFile> images
  ) {
    shopManageService.uploadTreatmentImages(shopId, treatmentId, images);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @Operation(summary = "매장 시술 이미지 삭제", description = "특정 시술의 특정 이미지를 삭제합니다.")
  @DeleteMapping("/{shopId}/treatments/{treatmentId}/images/{imageId}")
  public ResponseEntity<ApiResponse<Void>> deleteTreatmentImage(
          @Parameter(description = "매장 ID", required = true) @PathVariable Long shopId,
          @Parameter(description = "시술 ID", required = true) @PathVariable Long treatmentId,
          @Parameter(description = "삭제할 이미지 ID", required = true) @PathVariable Long imageId
  ) {
      shopManageService.deleteTreatmentImages(shopId, treatmentId, imageId);
      return ResponseEntity.ok(ApiResponse.success(null));
  }

  @Operation(summary = "매장 시술 삭제", description = "특정 시술을 삭제합니다. 시술에 연결된 모든 이미지와 옵션도 함께 삭제됩니다.")
  @DeleteMapping("/{shopId}/treatments/{treatmentId}")
  public ResponseEntity<ApiResponse<Void>> deleteTreatment(
      @Parameter(description = "매장 ID", required = true) @PathVariable Long shopId,
      @Parameter(description = "삭제할 시술 ID", required = true) @PathVariable Long treatmentId
  ) {
    shopManageService.deleteTreatment(shopId, treatmentId);
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