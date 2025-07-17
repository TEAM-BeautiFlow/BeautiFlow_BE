package com.beautiflow.shop.dto;

import com.beautiflow.global.domain.TreatmentCategory;
import com.beautiflow.global.domain.WeekDay;
import com.beautiflow.reservation.dto.response.TreatmentDetailWithOptionResponse.OptionGroupDto;
import java.time.LocalTime;
import java.util.List;
import lombok.Builder;

public record ShopDetailRes(
    Long id,
    String name,
    String contact,
    String location,
    String introText,
    String mainImageUrl,
    List<NoticeDto> notices,
    List<BusinessHourDto> businessHours,
    List<TreatmentDto> treatments

) {
    @Builder
    public ShopDetailRes {}

    public record NoticeDto(Long id, String title, String content) {
        @Builder
        public NoticeDto{}
    }

    public record BusinessHourDto(
            WeekDay dayOfWeek,
            LocalTime openTime,
            LocalTime closeTime,
            LocalTime breakStart,
            LocalTime breakEnd,
            boolean isClosed
    ) {
        @Builder
        public BusinessHourDto {}
    }

    public record TreatmentDto(
            Long id,
            TreatmentCategory category,
            String name,
            Integer price,
            Integer durationMinutes,
            String description,
            List<TreatmentImageDto> images,
            List<OptionGroupDto> optionGroups
    ) {

        @Builder
        public TreatmentDto {}

        public record TreatmentImageDto(Long id, String imageUrl) {

            @Builder
            public TreatmentImageDto {}
        }
        public record OptionGroupDto(
                Long id,
                String name,
                boolean enabled,
                List<OptionItemDto> items
        ) {
            @Builder
            public OptionGroupDto {}
        }

        public record OptionItemDto(
                Long id,
                String name,
                Integer extraMinutes,
                String description
        ) {
            @Builder
            public OptionItemDto {}
        }
    }
}
