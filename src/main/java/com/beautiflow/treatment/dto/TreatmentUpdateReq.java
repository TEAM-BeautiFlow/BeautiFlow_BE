package com.beautiflow.treatment.dto;

import com.beautiflow.global.domain.TreatmentCategory;
import java.util.List;

public record TreatmentUpdateReq(
    TreatmentCategory category,
    String name,
    Integer price,
    String description,
    Integer durationMinutes,
    List<Long> deleteImageIds
) {
}
