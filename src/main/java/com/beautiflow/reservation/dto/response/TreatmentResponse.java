package com.beautiflow.reservation.dto.response;

import com.beautiflow.treatment.domain.Treatment;
import java.util.List;

public record TreatmentResponse(
        Long id,
        String category,
        String name,
        Integer price,
        Integer durationMinutes,
        String description,
        List<TreatmentImageResponse> images
) {
    public static TreatmentResponse from(Treatment treatment) {
        return new TreatmentResponse(
                treatment.getId(),
                treatment.getCategory().name(),
                treatment.getName(),
                treatment.getPrice(),
                treatment.getDurationMinutes(),
                treatment.getDescription(),
                treatment.getImages().stream()
                        .map(img -> new TreatmentImageResponse(img.getId(), img.getImageUrl()))
                        .toList()
        );
    }
}
