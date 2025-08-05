package com.beautiflow.reservation.dto.response;

import com.beautiflow.treatment.domain.Treatment;
import java.util.List;

public record TreatmentRes(
        Long id,
        String category,
        String name,
        Integer price,
        Integer durationMinutes,
        String description,
        List<TreatmentImageRes> images
) {
    public static TreatmentRes from(Treatment treatment) {
        return new TreatmentRes(
                treatment.getId(),
                treatment.getCategory().name(),
                treatment.getName(),
                treatment.getPrice(),
                treatment.getDurationMinutes(),
                treatment.getDescription(),
                treatment.getImages().stream()
                        .map(img -> new TreatmentImageRes(img.getId(), img.getImageUrl()))
                        .toList()
        );
    }
}
