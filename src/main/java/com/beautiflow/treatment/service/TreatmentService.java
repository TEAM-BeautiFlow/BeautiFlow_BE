package com.beautiflow.treatment.service;

import com.beautiflow.global.common.error.ShopErrorCode;
import com.beautiflow.global.common.error.TreatmentErrorCode;
import com.beautiflow.global.common.exception.BeautiFlowException;
import com.beautiflow.global.common.s3.S3Service;
import com.beautiflow.global.common.s3.S3UploadResult;
import com.beautiflow.global.domain.TreatmentCategory;
import com.beautiflow.shop.repository.ShopRepository;
import com.beautiflow.treatment.dto.TreatmentInfoRes;
import com.beautiflow.treatment.domain.Treatment;
import com.beautiflow.treatment.domain.TreatmentImage;
import com.beautiflow.treatment.dto.TreatmentUpdateReq;
import com.beautiflow.treatment.repository.TreatmentImageRepository;
import com.beautiflow.treatment.repository.TreatmentRepository;
import java.util.List;
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

  // 시술 정보 수정
  @Transactional
  public TreatmentInfoRes updateTreatment(Long treatmentId, TreatmentUpdateReq requestDto, List<MultipartFile> newImages) {
    // 1. 수정할 시술 조회
    Treatment treatment = treatmentRepository.findById(treatmentId)
        .orElseThrow(() -> new BeautiFlowException(TreatmentErrorCode.TREATMENT_NOT_FOUND));

    int imagesToDeleteCount = (requestDto.deleteImageIds() != null) ? requestDto.deleteImageIds().size() : 0;
    int newImagesCount = (newImages != null) ? newImages.size() : 0;
    int currentImageCount = treatment.getImages().size();

    if (currentImageCount - imagesToDeleteCount + newImagesCount > MAX_IMAGE_COUNT) {
      throw new BeautiFlowException(TreatmentErrorCode.IMAGE_COUNT_EXCEEDED);
    }

    // 2. 기존 이미지 삭제
    if (requestDto.deleteImageIds() != null && !requestDto.deleteImageIds().isEmpty()) {
      deleteTreatmentImages(requestDto.deleteImageIds());
    }

    // 3. 새 이미지 업로드
    if (newImages != null && !newImages.isEmpty()) {
      Long shopId = treatment.getShop().getId();
      uploadNewTreatmentImages(treatment, newImages, shopId);
    }

    // 4. 텍스트 정보 업데이트
    treatment.updateTreatment(requestDto);

    // 5. 수정된 정보 반환
    return TreatmentInfoRes.from(treatment);
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
      S3UploadResult result = s3Service.uploadFile(file, s3Path);
      TreatmentImage newImage = TreatmentImage.builder()
          .treatment(treatment)
          .originalFileName(file.getOriginalFilename())
          .storedFilePath(result.fileKey())
          .imageUrl(result.imageUrl())
          .build();
      treatmentImageRepository.save(newImage);
    }
  }
}