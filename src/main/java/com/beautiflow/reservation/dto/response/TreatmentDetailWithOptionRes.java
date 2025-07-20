package com.beautiflow.reservation.dto.response;

import java.util.List;
import lombok.Builder;

@Builder
public record TreatmentDetailWithOptionRes(
        Long id,
        String name,
        Integer durationMinutes,
        Integer price,
        String description,
        List<TreatmentImageDto> images,
        List<OptionGroupDto> optionGroups
) {
    @Builder
    public record TreatmentImageDto(Long id, String imageUrl) {}

    @Builder
    public record OptionGroupDto(
            Long id,
            String name,
            boolean enabled,
            List<OptionItemDto> items
    ) {
        @Builder
        public record OptionItemDto(
                Long id,
                String name,
                Integer extraMinutes,
                String description
        ) {}
    }
}
