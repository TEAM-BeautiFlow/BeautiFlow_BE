package com.beautiflow.shop.controller;

import com.beautiflow.global.common.ApiResponse;
import com.beautiflow.global.domain.TreatmentCategory;
import com.beautiflow.shop.dto.ShopInfoRes;
import com.beautiflow.shop.dto.ShopUpdateReq;
import com.beautiflow.shop.service.ShopManageService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/shops/manage")
public class ShopManageController {

  private final ShopManageService shopManageService;

  // 매장 상세 정보 조회
  @GetMapping("/{shopId}")
  public ResponseEntity<ApiResponse<ShopInfoRes>> getShopDetails(@PathVariable Long shopId) {
    ShopInfoRes shopDetails = shopManageService.getShopDetails(shopId);

    return ResponseEntity.ok(ApiResponse.success(shopDetails));
  }

  // 매장 정보 및 이미지 수정
  @PatchMapping("/{shopId}")
  public ResponseEntity<ApiResponse<ShopInfoRes>> updateShopDetails(
      @PathVariable Long shopId,
      @ModelAttribute ShopUpdateReq requestDto,
      @RequestParam(value = "newImages", required = false) List<MultipartFile> newImages
  ) {
    ShopInfoRes updatedShop = shopManageService.updateShopDetailsAndImages(shopId, requestDto, newImages);
    return ResponseEntity.ok(ApiResponse.success(updatedShop));
  }

  // 매장 사업자 등록증 이미지 조회
  @GetMapping("/{shopId}/license-image")
  public ResponseEntity<ApiResponse<String>> getLicenseImage(@PathVariable Long shopId) {
    String licenseImageUrl = shopManageService.getLicenseImageUrl(shopId);
    return ResponseEntity.ok(ApiResponse.success(licenseImageUrl));
  }

  // 매장 사업자 등록증 이미지 업로드
  @PostMapping(value = "/{shopId}/license-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse<String>> uploadLicenseImage(
      @PathVariable Long shopId,
      @RequestParam(value = "licenseImage") MultipartFile licenseImage
  ){
    String licenseImageUrl = shopManageService.uploadLicenseImage(shopId, licenseImage);
    return ResponseEntity.ok(ApiResponse.success(licenseImageUrl));
  }





}