package com.beautiflow.treatment.service;

import com.beautiflow.global.common.error.ShopErrorCode;
import com.beautiflow.global.common.error.TreatmentErrorCode;
import com.beautiflow.global.common.exception.BeautiFlowException;
import com.beautiflow.global.common.s3.S3Service;
import com.beautiflow.global.common.s3.S3UploadResult;
import com.beautiflow.global.domain.TreatmentCategory;
import com.beautiflow.shop.dto.OptionGroupUpdateReq;
import com.beautiflow.shop.dto.OptionItemUpdateReq;
import com.beautiflow.shop.repository.ShopRepository;
import com.beautiflow.treatment.domain.OptionGroup;
import com.beautiflow.treatment.domain.OptionItem;
import com.beautiflow.treatment.dto.TreatmentInfoRes;
import com.beautiflow.treatment.domain.Treatment;
import com.beautiflow.treatment.domain.TreatmentImage;
import com.beautiflow.treatment.dto.TreatmentUpdateReq;
import com.beautiflow.reservation.repository.TreatmentImageRepository;
import com.beautiflow.reservation.repository.TreatmentRepository;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TreatmentService {

  private final TreatmentRepository treatmentRepository;
  private final TreatmentImageRepository treatmentImageRepository;
  private final ShopRepository shopRepository;
  private final S3Service s3Service; // S3 서비스 주입

  private static final int MAX_IMAGE_COUNT = 5;

  // 매장 시술 목록 조회
  public List<TreatmentInfoRes> getTreatments(Long shopId, TreatmentCategory category) {

    if (!shopRepository.existsById(shopId)) {
      throw new BeautiFlowException(ShopErrorCode.SHOP_NOT_FOUND);
    }

    List<Treatment> treatments = treatmentRepository.findByShopIdAndCategory(shopId, category);

    // 조회된 엔티티 리스트를 DTO 리스트로 변환
    return treatments.stream()
        .map(TreatmentInfoRes::from)
        .collect(Collectors.toList());
  }

  @Transactional
  public void updateTreatment(Long treatmentId, TreatmentUpdateReq requestDto,
      List<MultipartFile> newImages, List<Long> deleteImageIds) {

    // 1. 시술 엔티티 조회
    Treatment treatment = treatmentRepository.findById(treatmentId)
        .orElseThrow(() -> new BeautiFlowException(TreatmentErrorCode.TREATMENT_NOT_FOUND));

    // 2. 이미지 개수 검증
    int imagesToDeleteCount = (deleteImageIds != null) ? deleteImageIds.size() : 0;
    int newImagesCount = (newImages != null) ? newImages.size() : 0;
    int currentImageCount = treatment.getImages().size();

    if (currentImageCount - imagesToDeleteCount + newImagesCount > MAX_IMAGE_COUNT) {
      throw new BeautiFlowException(TreatmentErrorCode.IMAGE_COUNT_EXCEEDED);
    }

    // 3. 기존 이미지 삭제 (S3 및 DB)
    if (deleteImageIds != null && !deleteImageIds.isEmpty()) {
      deleteTreatmentImages(deleteImageIds);
    }

    // 4. 새 이미지 업로드 (S3 및 DB)
    if (newImages != null && !newImages.isEmpty()) {
      Long shopId = treatment.getShop().getId();
      uploadNewTreatmentImages(treatment, newImages, shopId);
    }

    // 5. 텍스트 정보 업데이트
    treatment.updateTreatment(requestDto);

    // 6. 옵션 그룹 및 아이템 정보 업데이트
    if (requestDto.optionGroups() != null) {
      updateOptionGroups(treatment, requestDto.optionGroups()); // 이전 답변의 헬퍼 메서드 재사용
    }
  }

  private void updateOptionGroups(Treatment treatment, List<OptionGroupUpdateReq> groupDtos) {
    Map<Long, OptionGroup> existingGroupsMap = treatment.getOptionGroups().stream()
        .collect(Collectors.toMap(OptionGroup::getId, Function.identity()));

    for (OptionGroupUpdateReq groupDto : groupDtos) {
      OptionGroup optionGroup;
      if (groupDto.id() == null) {
        // ID가 없으면: 신규 그룹 생성 및 추가
        optionGroup = OptionGroup.builder()
            .name(groupDto.name())
            .enabled(true) // 기본값 혹은 DTO에 따라 설정
            .treatment(treatment)
            .build();
        treatment.getOptionGroups().add(optionGroup);
      } else {
        // ID가 있으면: 기존 그룹 수정
        optionGroup = existingGroupsMap.get(groupDto.id());
        if (optionGroup != null) {
          optionGroup.setName(groupDto.name()); // 그룹 이름 등 업데이트
          existingGroupsMap.remove(groupDto.id()); // 처리된 그룹은 Map에서 제거
        } else {
          throw new BeautiFlowException(TreatmentErrorCode.OPTION_GROUP_NOT_FOUND);
        }
      }

      // 해당 그룹의 아이템 목록 업데이트
      if (groupDto.items() != null) {
        updateOptionItems(optionGroup, groupDto.items());
      }
    }
    // Map에 남아있는 그룹 = 요청에서 누락된 그룹 = 삭제 대상
    treatment.getOptionGroups().removeAll(existingGroupsMap.values());
  }

  private void updateOptionItems(OptionGroup optionGroup, List<OptionItemUpdateReq> itemDtos) {
    Map<Long, OptionItem> existingItemsMap = optionGroup.getItems().stream()
        .collect(Collectors.toMap(OptionItem::getId, Function.identity()));

    for (OptionItemUpdateReq itemDto : itemDtos) {
      if (itemDto.id() == null) {
        // ID가 없으면: 신규 아이템 생성 및 추가
        OptionItem newItem = OptionItem.builder()
            .name(itemDto.name())
            .extraPrice(itemDto.extraPrice())
            .extraMinutes(itemDto.extraMinutes())
            .description(itemDto.description())
            .optionGroup(optionGroup)
            .build();
        optionGroup.getItems().add(newItem);
      } else {
        // ID가 있으면: 기존 아이템 수정
        OptionItem existingItem = existingItemsMap.get(itemDto.id());
        if (existingItem != null) {
          existingItem.updateDetails(
              itemDto.name(),
              itemDto.extraPrice(),
              itemDto.extraMinutes(),
              itemDto.description()
          );

          existingItemsMap.remove(itemDto.id());
        }
      }
    }
    // Map에 남아있는 아이템 = 삭제 대상
    optionGroup.getItems().removeAll(existingItemsMap.values());
  }

  // 시술 이미지 삭제 시
  private void deleteTreatmentImages(List<Long> imageIds) {
    List<TreatmentImage> imagesToDelete = treatmentImageRepository.findAllById(imageIds);

    if (imagesToDelete.size() != imageIds.size()) {
      throw new BeautiFlowException(ShopErrorCode.IMAGE_NOT_FOUND);
    }

    for (TreatmentImage image : imagesToDelete) {
      s3Service.deleteFile(image.getStoredFilePath());
    }
    treatmentImageRepository.deleteAll(imagesToDelete);
  }

  // 시술 이미지 업로드 시
  private void uploadNewTreatmentImages(Treatment treatment, List<MultipartFile> files, Long shopId) {
    String s3Path = String.format("shops/%d/treatments", shopId);

    for (MultipartFile file : files) {
      try {
        S3UploadResult result = s3Service.uploadFile(file, s3Path);
        TreatmentImage newImage = TreatmentImage.builder()
            .treatment(treatment)
            .originalFileName(file.getOriginalFilename())
            .storedFilePath(result.fileKey())
            .imageUrl(result.imageUrl())
            .build();
        treatmentImageRepository.save(newImage);
      } catch (Exception e) {
        throw new BeautiFlowException(TreatmentErrorCode.S3_UPLOAD_FAILED);
      }
    }
  }
}