package com.beautiflow.shop.service;

import com.beautiflow.shop.domain.Shop;
import com.beautiflow.shop.domain.ShopImage;
import com.beautiflow.shop.dto.ShopInfoResponseDto;
import com.beautiflow.shop.dto.ShopUpdateRequestDto;
import com.beautiflow.shop.repository.ShopImageRepository;
import com.beautiflow.shop.repository.ShopRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ShopService {

  private final ShopRepository shopRepository;
  private final ShopImageRepository shopImageRepository;

  @Transactional(readOnly = true)
  public ShopInfoResponseDto getShopDetails(Long shopId) {
    Shop shop = shopRepository.findById(shopId)
        .orElseThrow(() -> new IllegalArgumentException("해당 매장을 찾을 수 없습니다. id=" + shopId));

    List<String> shopImageUrls = shop.getShopImages().stream()
        .map(ShopImage::getImageUrl)
        .collect(Collectors.toList());

    return new ShopInfoResponseDto(
        shop.getId(),
        shopImageUrls,
        shop.getShopName(),
        shop.getContact(),
        shop.getLink(),
        shop.getAccountInfo(),
        shop.getAddress(),
        shop.getIntroduction()
    );
  }

  @Transactional
  public ShopInfoResponseDto updateShopDetailsAndImages(
      Long shopId,
      ShopUpdateRequestDto requestDto,
      List<MultipartFile> newImages
  ) {
    Shop shop = shopRepository.findById(shopId)
        .orElseThrow(() -> new IllegalArgumentException("해당 매장을 찾을 수 없습니다. id=" + shopId));

    // 1. 기존 이미지 삭제 처리
    if (requestDto.deleteImageIds() != null && !requestDto.deleteImageIds().isEmpty()) {
      deleteImages(requestDto.deleteImageIds());
    }

    // 2. 새로운 이미지 추가 처리
    if (newImages != null && !newImages.isEmpty()) {
      uploadNewImages(shop, newImages);
    }

    // 3. 텍스트 정보 수정 (PATCH 방식)
    if (requestDto.shopName() != null) {
      shop.setShopName(requestDto.shopName());
    }
    if (requestDto.contact() != null) {
      shop.setContact(requestDto.contact());
    }
    if (requestDto.link() != null) {
      shop.setLink(requestDto.link());
    }
    if (requestDto.accountInfo() != null) {
      shop.setAccountInfo(requestDto.accountInfo());
    }
    if (requestDto.address() != null) {
      shop.setAddress(requestDto.address());
    }
    if (requestDto.introduction() != null) {
      shop.setIntroduction(requestDto.introduction());
    }

    // 4. 수정된 최신 정보를 DTO로 변환하여 반환
    List<String> updatedShopImageUrls = shop.getShopImages().stream()
        .map(ShopImage::getImageUrl)
        .collect(Collectors.toList());

    return new ShopInfoResponseDto(
        shop.getId(),
        updatedShopImageUrls,
        shop.getShopName(),
        shop.getContact(),
        shop.getLink(),
        shop.getAccountInfo(),
        shop.getAddress(),
        shop.getIntroduction()
    );
  }

  /**
   * 이미지 삭제 헬퍼 메서드
   */
  private void deleteImages(List<Long> imageIds) {
    for (Long imageId : imageIds) {
      ShopImage image = shopImageRepository.findById(imageId)
          .orElseThrow(() -> new IllegalArgumentException("삭제할 이미지를 찾을 수 없습니다. id=" + imageId));

      // s3Service.deleteFile(image.getStoredFilePath()); // S3에서 파일 삭제 (가정)
      shopImageRepository.delete(image); // DB에서 이미지 정보 삭제
    }
  }

  /**
   * 이미지 추가 헬퍼 메서드
   */
  private void uploadNewImages(Shop shop, List<MultipartFile> newImages) {
    for (MultipartFile file : newImages) {
      // S3 업로드 로직 (가정)
      // S3UploadResult result = s3Service.uploadFile(file, "shops/images");

      // ShopImage newImage = ShopImage.builder()
      //         .shop(shop)
      //         .originalFileName(file.getOriginalFilename())
      //         .storedFilePath(result.getFileKey())
      //         .imageUrl(result.getImageUrl())
      //         .build();
      // shop.getShopImages().add(newImage); // 연관관계 편의 메서드를 사용하거나 직접 추가
      // shopImageRepository.save(newImage);
    }
  }
}