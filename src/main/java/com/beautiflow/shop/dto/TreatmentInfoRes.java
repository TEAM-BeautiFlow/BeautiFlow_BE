package com.beautiflow.shop.dto;

import com.beautiflow.global.domain.TreatmentCategory;
import com.beautiflow.treatment.domain.Treatment;

public record TreatmentInfoRes(
    Long treatmentId,
    String name,
    TreatmentCategory category,
    String description,
    Integer minPrice,
    Integer maxPrice,
    Integer durationMinutes,
    String imageUrl
) {
  public static TreatmentInfoRes from(Treatment treatment) {
    // 이미지가 없을 경우 null 처리
    String representativeImageUrl = treatment.getImages().isEmpty()
        ? null
        : treatment.getImages().get(0).getImageUrl();

    // record의 생성자를 사용하여 객체 생성
    return new TreatmentInfoRes(
        treatment.getId(),
        treatment.getName(),
        treatment.getCategory(),
        treatment.getDescription(),
        treatment.getMinPrice(),
        treatment.getMaxPrice(),
        treatment.getDurationMinutes(),
        representativeImageUrl
    );
  }
}