package com.beautiflow.shop.service;

import com.beautiflow.global.common.error.ShopErrorCode;
import com.beautiflow.global.common.error.TreatmentErrorCode;
import com.beautiflow.global.common.exception.BeautiFlowException;
import com.beautiflow.global.common.s3.S3Service;
import com.beautiflow.global.common.s3.S3UploadResult;
import com.beautiflow.global.domain.HolidayCycle;
import com.beautiflow.global.domain.WeekDay;
import com.beautiflow.reservation.repository.TreatmentImageRepository;
import com.beautiflow.reservation.repository.TreatmentRepository;
import com.beautiflow.shop.domain.BusinessHour;
import com.beautiflow.shop.domain.RegularHoliday;
import com.beautiflow.shop.domain.Shop;
import com.beautiflow.shop.domain.ShopImage;
import com.beautiflow.shop.dto.BusinessHourRes;
import com.beautiflow.shop.dto.BusinessHourUpdateReq;
import com.beautiflow.shop.dto.RegularHolidayDto;
import com.beautiflow.shop.dto.ShopInfoRes;
import com.beautiflow.shop.dto.ShopUpdateReq;
import com.beautiflow.shop.dto.TreatmentUpsertReq;
import com.beautiflow.shop.dto.TreatmentUpsertRes;
import com.beautiflow.shop.repository.ShopImageRepository;
import com.beautiflow.shop.repository.ShopRepository;
import com.beautiflow.treatment.domain.Treatment;
import com.beautiflow.treatment.domain.TreatmentImage;
import com.beautiflow.treatment.dto.TreatmentUpdateReq;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
  private final TreatmentRepository treatmentRepository;
  private final TreatmentImageRepository treatmentImageRepository;

  // 매장 상세 정보 조회
  @Transactional(readOnly = true)
  public ShopInfoRes getShopDetails(Long shopId) {
    Shop shop = shopRepository.findById(shopId)
        .orElseThrow(() -> new BeautiFlowException(ShopErrorCode.SHOP_NOT_FOUND));

    return ShopInfoRes.from(shop);
  }

  // 매장 정보 및 이미지 수정
  @Transactional
  public ShopInfoRes updateShopDetailsAndImages(
      Long shopId,
      ShopUpdateReq requestDto,
      List<MultipartFile> newImages
  ) {
    Shop shop = shopRepository.findById(shopId)
        .orElseThrow(() -> new BeautiFlowException(ShopErrorCode.SHOP_NOT_FOUND));

    if (requestDto.deleteImageIds() != null && !requestDto.deleteImageIds().isEmpty()) {
      deleteImages(shop, requestDto.deleteImageIds());
    }

    if (newImages != null && !newImages.isEmpty()) {
      uploadNewImages(shop, newImages);
    }

    shop.updateDetails(requestDto);

    return ShopInfoRes.from(shop);
  }

  // 매장 영업 시간 조회
  @Transactional(readOnly = true)
  public BusinessHourRes getBusinessHours(Long shopId) {
    Shop shop = shopRepository.findById(shopId)
        .orElseThrow(() -> new BeautiFlowException(ShopErrorCode.SHOP_NOT_FOUND));

    List<BusinessHour> businessHours = shop.getBusinessHours();

    return BusinessHourRes.from(businessHours);
  }

  // 매장 정기 휴무 수정
  @Transactional
  public void updateRegularHolidays(Long shopId, List<RegularHolidayDto> requestDtos) {
    Shop shop = shopRepository.findById(shopId)
        .orElseThrow(() -> new BeautiFlowException(ShopErrorCode.SHOP_NOT_FOUND));

    shop.getRegularHolidays().clear();

    if (requestDtos != null && !requestDtos.isEmpty()) {
      Map<HolidayCycle, List<WeekDay>> cycleMap = requestDtos.stream()
          .collect(Collectors.toMap(
              RegularHolidayDto::getCycle,
              RegularHolidayDto::getDaysOfWeek,
              (existing, replacement) -> replacement
          ));

      List<RegularHoliday> newHolidays = cycleMap.entrySet().stream()
          .map(entry -> RegularHoliday.builder()
              .shop(shop)
              .cycle(entry.getKey())
              .daysOfWeek(entry.getValue())
              .build())
          .toList();

      shop.getRegularHolidays().addAll(newHolidays);
    }
  }

  // 매장 정기 휴무 조회
  @Transactional(readOnly = true)
  public List<RegularHolidayDto> getRegularHolidays(Long shopId) {
    Shop shop = shopRepository.findById(shopId)
        .orElseThrow(() -> new BeautiFlowException(ShopErrorCode.SHOP_NOT_FOUND));

    return shop.getRegularHolidays().stream()
        .map(RegularHolidayDto::from)
        .collect(Collectors.toList());
  }

  // 매장 영업 시간 수정
  @Transactional
  public void updateBusinessHours(Long shopId, BusinessHourUpdateReq requestDto) {
    Shop shop = shopRepository.findById(shopId)
        .orElseThrow(() -> new BeautiFlowException(ShopErrorCode.SHOP_NOT_FOUND));

    shop.getBusinessHours().clear();

    List<BusinessHour> newBusinessHours = EnumSet.allOf(WeekDay.class).stream()
        .map(day ->
            BusinessHour.builder()
              .shop(shop)
              .dayOfWeek(day)
              .isClosed(false)
              .openTime(requestDto.openTime())
              .closeTime(requestDto.closeTime())
              .breakStart(requestDto.breakStart())
              .breakEnd(requestDto.breakEnd())
              .build()
        )
        .toList();

    shop.getBusinessHours().addAll(newBusinessHours);
  }

  // 시술 정보 생성/수정 (이미지 제외)
  @Transactional
  public List<TreatmentUpsertRes> upsertTreatmentsWithoutImages(Long shopId, List<TreatmentUpsertReq> requestDtos) {
    Shop shop = shopRepository.findById(shopId)
        .orElseThrow(() -> new BeautiFlowException(ShopErrorCode.SHOP_NOT_FOUND));

    List<Treatment> savedTreatments = requestDtos.stream()
        .map(dto -> {
          if (dto.id() == null) {
            Treatment newTreatment = Treatment.builder()
                .shop(shop)
                .category(dto.category())
                .name(dto.name())
                .price(dto.price())
                .durationMinutes(dto.durationMinutes())
                .description(dto.description())
                .build();
            return treatmentRepository.save(newTreatment);
          } else {
            Treatment existingTreatment = treatmentRepository.findByShopAndId(shop, dto.id())
                .orElseThrow(() -> new BeautiFlowException(TreatmentErrorCode.TREATMENT_NOT_FOUND));

            TreatmentUpdateReq updateDto = new TreatmentUpdateReq(
                dto.category(), dto.name(), dto.price(),
                dto.durationMinutes(), dto.description(), dto.optionGroups()
            );
            existingTreatment.updateTreatment(updateDto);
            return existingTreatment;
          }
        })
        .toList();

    return savedTreatments.stream()
        .map(TreatmentUpsertRes::from)
        .collect(Collectors.toList());
  }

  // 시술 이미지 업로드
  @Transactional
  public void uploadTreatmentImages(Long shopId, Long treatmentId, List<MultipartFile> images) {
    Shop shop = shopRepository.findById(shopId)
        .orElseThrow(() -> new BeautiFlowException(ShopErrorCode.SHOP_NOT_FOUND));
    Treatment treatment = treatmentRepository.findByShopAndId(shop, treatmentId)
        .orElseThrow(() -> new BeautiFlowException(TreatmentErrorCode.TREATMENT_NOT_FOUND));

    if (images != null && !images.isEmpty()) {
      uploadNewTreatmentImages(treatment, images);
    }
  }

  // 시술 이미지 삭제
  @Transactional
  public void deleteTreatmentImages(Long shopId, Long treatmentId, Long imageId) {
    if (!treatmentRepository.existsByIdAndShopId(treatmentId, shopId)) {
      throw new BeautiFlowException(TreatmentErrorCode.TREATMENT_NOT_FOUND);
    }

    TreatmentImage image = treatmentImageRepository.findById(imageId)
        .orElseThrow(() -> new BeautiFlowException(ShopErrorCode.IMAGE_NOT_FOUND));

    if (!image.getTreatment().getId().equals(treatmentId)) {
      throw new BeautiFlowException(ShopErrorCode.UNAUTHORIZED_SHOP_ACCESS);
    }

    if (image.getStoredFilePath() != null && !image.getStoredFilePath().isEmpty()) {
      s3Service.deleteFile(image.getStoredFilePath());
    }

    treatmentImageRepository.delete(image);
  }

  // 새로운 시술 이미지 업로드
  private void uploadNewTreatmentImages(Treatment treatment, List<MultipartFile> newImages) {
    for (MultipartFile file : newImages) {
      String dirName = String.format("shops/%d/treatments/%d", treatment.getShop().getId(), treatment.getId());

      S3UploadResult result = s3Service.uploadFile(file, dirName);

      TreatmentImage newImage = TreatmentImage.builder()
          .treatment(treatment)
          .originalFileName(file.getOriginalFilename())
          .storedFilePath(result.fileKey())
          .imageUrl(result.imageUrl())
          .build();

      treatment.getImages().add(newImage);

      treatmentImageRepository.save(newImage);
    }
  }

  // 시술 삭제
  @Transactional
  public void deleteTreatment(Long shopId, Long treatmentId) {
    List<Treatment> treatments = treatmentRepository.findByShopIdAndId(shopId, treatmentId);

    if (treatments.isEmpty()) {
      throw new BeautiFlowException(TreatmentErrorCode.TREATMENT_NOT_FOUND);
    }

    Treatment treatment = treatments.get(0);

    treatment.getImages().forEach(image -> {
      s3Service.deleteFile(image.getStoredFilePath());
    });

    treatmentRepository.delete(treatment);
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
    if (!shopRepository.existsById(shopId)) {
      throw new BeautiFlowException(ShopErrorCode.SHOP_NOT_FOUND);
    }

    String dirName = String.format("shops/%d/license", shopId);

    S3UploadResult result = s3Service.uploadFile(licenseImage, dirName);

    try {
      updateLicenseImageUrl(shopId, result.imageUrl());
      return result.imageUrl();
    } catch (Exception e) {
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
    for (Long imageId : imageIdsToDelete) {
      ShopImage imageToRemove = shop.getShopImages().stream()
          .filter(shopImage -> shopImage.getId().equals(imageId))
          .findFirst()
          .orElseThrow(() -> new BeautiFlowException(ShopErrorCode.IMAGE_NOT_FOUND));

      s3Service.deleteFile(imageToRemove.getStoredFilePath());
      shop.getShopImages().remove(imageToRemove);
    }
  }

  // 새로운 이미지 업로드
  private void uploadNewImages(Shop shop, List<MultipartFile> newImages) {
    for (MultipartFile file : newImages) {
      S3UploadResult result = s3Service.uploadFile(file, "shops/images");

      ShopImage newImage = ShopImage.builder()
          .shop(shop)
          .originalFileName(file.getOriginalFilename())
          .storedFilePath(result.fileKey())
          .imageUrl(result.imageUrl())
          .build();

      shop.getShopImages().add(newImage);
      shopImageRepository.save(newImage);
    }
  }
}