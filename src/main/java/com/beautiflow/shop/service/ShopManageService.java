package com.beautiflow.shop.service;

import com.beautiflow.global.common.error.ShopErrorCode;
import com.beautiflow.global.common.error.TreatmentErrorCode;
import com.beautiflow.global.common.exception.BeautiFlowException;
import com.beautiflow.global.common.s3.S3Service;
import com.beautiflow.global.common.s3.S3UploadResult;
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

  @Transactional(readOnly = true)
  public BusinessHourRes getBusinessHours(Long shopId) {
    Shop shop = shopRepository.findById(shopId)
        .orElseThrow(() -> new BeautiFlowException(ShopErrorCode.SHOP_NOT_FOUND));

    List<BusinessHour> businessHours = shop.getBusinessHours();

    // BusinessHour 엔티티 리스트를 BusinessHourRes DTO로 변환
    return BusinessHourRes.from(businessHours);
  }

  @Transactional
  public void updateRegularHolidays(Long shopId, List<RegularHolidayDto> requestDtos) {
    Shop shop = shopRepository.findById(shopId)
        .orElseThrow(() -> new BeautiFlowException(ShopErrorCode.SHOP_NOT_FOUND));

    // 기존 정기 휴무 규칙 모두 삭제
    shop.getRegularHolidays().clear();

    // 새로운 규칙으로 다시 설정
    if (requestDtos != null) {
      List<RegularHoliday> newHolidays = requestDtos.stream()
          .map(dto -> RegularHoliday.builder()
              .shop(shop)
              .cycle(dto.getCycle())
              .dayOfWeek(dto.getDayOfWeek())
              .build())
          .toList();
      shop.getRegularHolidays().addAll(newHolidays);
    }
  }

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

    List<WeekDay> closedDays = requestDto.regularClosedDays();

    List<BusinessHour> newBusinessHours = EnumSet.allOf(WeekDay.class).stream()
        .map(day -> {
          boolean isClosed = closedDays != null && closedDays.contains(day);
          return BusinessHour.builder()
              .shop(shop)
              .dayOfWeek(day)
              .isClosed(isClosed)
              .openTime(isClosed ? null : requestDto.openTime())
              .closeTime(isClosed ? null : requestDto.closeTime())
              .breakStart(isClosed ? null : requestDto.breakStart())
              .breakEnd(isClosed ? null : requestDto.breakEnd())
              .build();
        })
        .toList();

    shop.getBusinessHours().addAll(newBusinessHours);
  }

  @Transactional
  public List<TreatmentUpsertRes> upsertTreatmentsWithoutImages(Long shopId, List<TreatmentUpsertReq> requestDtos) {
    Shop shop = shopRepository.findById(shopId)
        .orElseThrow(() -> new BeautiFlowException(ShopErrorCode.SHOP_NOT_FOUND));

    List<Treatment> savedTreatments = requestDtos.stream()
        .map(dto -> {
          if (dto.id() == null) {
            // 신규 생성
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
            // 기존 수정
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

  // [!!!] 2. 이미지만 업로드하는 새로운 서비스 메서드
  @Transactional
  public void uploadTreatmentImages(Long shopId, Long treatmentId, List<MultipartFile> images) {
    Shop shop = shopRepository.findById(shopId)
        .orElseThrow(() -> new BeautiFlowException(ShopErrorCode.SHOP_NOT_FOUND));
    Treatment treatment = treatmentRepository.findByShopAndId(shop, treatmentId)
        .orElseThrow(() -> new BeautiFlowException(TreatmentErrorCode.TREATMENT_NOT_FOUND));

    if (images != null && !images.isEmpty()) {
      // 이전에 구현했던 이미지 업로드 헬퍼 메서드를 호출합니다.
      uploadNewTreatmentImages(treatment, images);
    }
  }

  @Transactional
  public void deleteTreatmentImages(Long shopId, Long treatmentId, Long imageId) {
    // 1. 매장과 시술 정보가 유효한지 먼저 확인 (보안 강화)
    if (!treatmentRepository.existsByIdAndShopId(treatmentId, shopId)) {
      throw new BeautiFlowException(TreatmentErrorCode.TREATMENT_NOT_FOUND);
    }

    // 2. 삭제할 이미지 엔티티를 찾음
    TreatmentImage image = treatmentImageRepository.findById(imageId)
        .orElseThrow(() -> new BeautiFlowException(ShopErrorCode.IMAGE_NOT_FOUND));

    // 3. (중요) 해당 이미지가 올바른 시술에 속해 있는지 다시 한번 확인
    if (!image.getTreatment().getId().equals(treatmentId)) {
      // 다른 시술의 이미지 ID를 추측해서 삭제하려는 시도를 방지
      throw new BeautiFlowException(ShopErrorCode.UNAUTHORIZED_SHOP_ACCESS);
    }

    // 4. S3에서 파일 삭제
    if (image.getStoredFilePath() != null && !image.getStoredFilePath().isEmpty()) {
      s3Service.deleteFile(image.getStoredFilePath());
    }

    // 5. DB에서 이미지 정보 삭제
    treatmentImageRepository.delete(image);
  }

  private void uploadNewTreatmentImages(Treatment treatment, List<MultipartFile> newImages) {
    for (MultipartFile file : newImages) {
      String dirName = String.format("shops/%d/treatments/%d", treatment.getShop().getId(), treatment.getId());

      // 2. S3에 파일 업로드
      S3UploadResult result = s3Service.uploadFile(file, dirName);

      // 3. DB에 저장할 TreatmentImage 엔티티 생성
      TreatmentImage newImage = TreatmentImage.builder()
          .treatment(treatment)
          .originalFileName(file.getOriginalFilename())
          .storedFilePath(result.fileKey())
          .imageUrl(result.imageUrl())
          .build();

      // 4. 연관관계 설정 (JPA가 관리하도록 리스트에 추가)
      treatment.getImages().add(newImage);

      treatmentImageRepository.save(newImage);
    }
  }

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