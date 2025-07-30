package com.beautiflow.shop.service;

import com.beautiflow.global.common.error.ShopErrorCode;
import com.beautiflow.global.common.exception.BeautiFlowException;
import com.beautiflow.global.common.error.CommonErrorCode;
import com.beautiflow.global.common.s3.S3Service;
import com.beautiflow.global.common.s3.S3UploadResult;
import com.beautiflow.shop.domain.Shop;
import com.beautiflow.shop.domain.ShopImage;
import com.beautiflow.shop.dto.ShopInfoRes;
import com.beautiflow.shop.dto.ShopUpdateReq;
import com.beautiflow.shop.repository.ShopImageRepository;
import com.beautiflow.shop.repository.ShopRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ShopManageService {

  private final ShopRepository shopRepository;
  private final ShopImageRepository shopImageRepository;
  private final S3Service s3Service;

  @Transactional(readOnly = true)
  public ShopInfoRes getShopDetails(Long shopId) {
    Shop shop = shopRepository.findById(shopId)
        .orElseThrow(() -> new BeautiFlowException(ShopErrorCode.SHOP_NOT_FOUND));

    return ShopInfoRes.from(shop);
  }

  @Transactional
  public ShopInfoRes updateShopDetailsAndImages(
      Long shopId,
      ShopUpdateReq requestDto,
      List<MultipartFile> newImages
  ) {
    Shop shop = shopRepository.findById(shopId)
        .orElseThrow(() -> new BeautiFlowException(ShopErrorCode.SHOP_NOT_FOUND));

    // 1. 기존 이미지 삭제 처리
    if (requestDto.deleteImageIds() != null && !requestDto.deleteImageIds().isEmpty()) {
      deleteImages(shop, requestDto.deleteImageIds());
    }

    // 2. 새로운 이미지 추가 처리
    if (newImages != null && !newImages.isEmpty()) {
      uploadNewImages(shop, newImages);
    }

    // 3. 샵 정보 업데이트
    shop.updateDetails(requestDto);

    return ShopInfoRes.from(shop);
  }

  // 매장 사업자 등록증 이미지 조회
  @Transactional
  public String getLicenseImageUrl(Long shopId) {
    Shop shop = shopRepository.findById(shopId)
        .orElseThrow(() -> new BeautiFlowException(ShopErrorCode.SHOP_NOT_FOUND));

    String licenseImageUrl = shop.getLicenseImageUrl();
    if (licenseImageUrl == null || licenseImageUrl.isEmpty()) {
      throw new BeautiFlowException(ShopErrorCode.IMAGE_NOT_FOUND);
    }

    return licenseImageUrl;
  }

  // 매장 사업자 등록증 이미지 업로드
  @Transactional
  public String uploadLicenseImage(Long shopId, MultipartFile licenseImage) {
    // 1. DB 트랜잭션 시작 전에 매장 존재 여부 확인
    if (!shopRepository.existsById(shopId)) {
      throw new BeautiFlowException(ShopErrorCode.SHOP_NOT_FOUND);
    }

    // 2. shopId를 기반으로 S3 저장 경로 생성
    String dirName = String.format("shops/%d/license", shopId);

    // 3. S3에 파일 먼저 업로드
    S3UploadResult result = s3Service.uploadFile(licenseImage, dirName);

    // 4. DB 업데이트 로직만 트랜잭션으로 호출
    try {
      updateLicenseImageUrl(shopId, result.imageUrl());
      return result.imageUrl();
    } catch (Exception e) {
      // 5. DB 업데이트 실패 시 업로드한 S3 파일을 삭제
      s3Service.deleteFile(result.fileKey());
      throw e;
    }
  }

  // DB 업데이트 로직 분리
  @Transactional
  public void updateLicenseImageUrl(Long shopId, String imageUrl) {
    Shop shop = shopRepository.findById(shopId)
        .orElseThrow(() -> new BeautiFlowException(ShopErrorCode.SHOP_NOT_FOUND));
    shop.setLicenseImageUrl(imageUrl);
  }

  // 이미지 삭제
  private void deleteImages(Shop shop, List<Long> imageIdsToDelete) {
    // 삭제할 이미지 ID에 대해 유효성 검사 수행
    for (Long imageId : imageIdsToDelete) {
      // 해당 ID 이미지 찾기
      ShopImage imageToRemove = shop.getShopImages().stream()
          .filter(shopImage -> shopImage.getId().equals(imageId))
          .findFirst()
          .orElseThrow(() -> new BeautiFlowException(ShopErrorCode.IMAGE_NOT_FOUND));

      // S3에 있는 실제 파일 삭제
      s3Service.deleteFile(imageToRemove.getStoredFilePath());
      shop.getShopImages().remove(imageToRemove);
    }
  }

  // 새로운 이미지 업로드
  private void uploadNewImages(Shop shop, List<MultipartFile> newImages) {
    for (MultipartFile file : newImages) {
      // S3에 파일 업로드
      S3UploadResult result = s3Service.uploadFile(file, "shops/images");

      // DB에 저장할 ShopImage 엔티티 생성
      ShopImage newImage = ShopImage.builder()
          .shop(shop)
          .originalFileName(file.getOriginalFilename())
          .storedFilePath(result.fileKey())
          .imageUrl(result.imageUrl())
          .build();

      // 연관관계 설정 및 DB 저장
      shop.getShopImages().add(newImage);
      shopImageRepository.save(newImage);
    }
  }
}