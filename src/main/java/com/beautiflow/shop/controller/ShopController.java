package com.beautiflow.shop.controller;

import com.beautiflow.global.common.ApiResponse;
import com.beautiflow.global.common.error.CommonErrorCode;
import com.beautiflow.global.common.error.ErrorCode;
import com.beautiflow.shop.dto.ShopInfoResponseDto;
import com.beautiflow.shop.dto.ShopUpdateRequestDto;
import com.beautiflow.shop.service.ShopService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ShopController {

  private final ShopService shopService;

  @GetMapping("/shops/{shopId}")
  public ResponseEntity<ApiResponse<ShopInfoResponseDto>> getShopDetails(@PathVariable Long shopId) {
    ShopInfoResponseDto shopDetails = shopService.getShopDetails(shopId);

    return ResponseEntity.ok(ApiResponse.success(shopDetails));
  }

  @PatchMapping(value = "/shops/{shopId}",consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
  public ResponseEntity<ApiResponse<ShopInfoResponseDto>> updateShopDetails(
      @PathVariable Long shopId,
      @RequestPart("requestDto") ShopUpdateRequestDto requestDto,
      @RequestPart(value = "newImages", required = false) List<MultipartFile> newImages
  ) {
    ShopInfoResponseDto updatedShop = shopService.updateShopDetailsAndImages(shopId, requestDto,newImages);

    return ResponseEntity.ok(ApiResponse.success(updatedShop));
  }
}