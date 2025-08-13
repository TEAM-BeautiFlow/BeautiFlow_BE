package com.beautiflow.shop.dto;

import com.beautiflow.global.domain.TreatmentCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.List;

public record TreatmentUpsertReq(
    Long id,

    @NotNull(message = "카테고리는 필수입니다.")
    TreatmentCategory category,

    @NotBlank(message = "시술명은 필수입니다.")
    String name,

    @NotNull(message = "가격은 필수입니다.")
    @PositiveOrZero(message = "가격은 0 이상이어야 합니다.")
    Integer price,

    @NotNull(message = "소요 시간은 필수입니다.")
    @Positive(message = "소요 시간은 0보다 커야 합니다.")
    Integer durationMinutes,

    String description,

    List<OptionGroupUpdateReq> optionGroups
) {}