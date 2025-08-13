package com.beautiflow.treatment.dto;

import com.beautiflow.global.domain.TreatmentCategory;
import com.beautiflow.treatment.domain.Treatment;

public record TreatmentInfoRes(
    Long treatmentId,
    String name,
    TreatmentCategory category,
    String description,
    Integer price,
    Integer durationMinutes,
    String imageUrl
) {
  public static TreatmentInfoRes from(Treatment treatment) {
    String representativeImageUrl = treatment.getImages().isEmpty()
        ? null
        : treatment.getImages().get(0).getImageUrl();

    return new TreatmentInfoRes(
        treatment.getId(),
        treatment.getName(),
        treatment.getCategory(),
        treatment.getDescription(),
        treatment.getPrice(),
        treatment.getDurationMinutes(),
        representativeImageUrl
    );
  }
}