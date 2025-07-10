package com.beautiflow.reservation.dto.response;

import com.beautiflow.global.domain.TreatmentCategory;
import com.beautiflow.treatment.domain.Treatment;
import java.util.List;
import lombok.Builder;

public record TreatmentResponse(
        Long id,
        String category,
        String name,
        Integer minPrice,
        Integer maxPrice,
        Integer durationMinutes,
        String description,
        List<TreatmentImageResponse> images
) {
    public static TreatmentResponse from(Treatment treatment) {
        return new TreatmentResponse(
                treatment.getId(),
                treatment.getCategory().name(),
                treatment.getName(),
                treatment.getMinPrice(),
                treatment.getMaxPrice(),
                treatment.getDurationMinutes(),
                treatment.getDescription(),
                treatment.getImages().stream()
                        .map(img -> new TreatmentImageResponse(img.getId(), img.getImageUrl()))
                        .toList()
        );
    }
}
