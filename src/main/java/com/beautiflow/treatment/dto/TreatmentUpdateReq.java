package com.beautiflow.treatment.dto;

import com.beautiflow.global.domain.TreatmentCategory;
import com.beautiflow.shop.dto.OptionGroupUpdateReq;
import java.util.List;

public record TreatmentUpdateReq(
    TreatmentCategory category,
    String name,
    Integer price,
    Integer durationMinutes,
    String description,

    List<OptionGroupUpdateReq> optionGroups
) {}