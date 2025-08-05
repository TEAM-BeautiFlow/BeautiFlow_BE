package com.beautiflow.shop.dto;

import com.beautiflow.global.domain.TreatmentCategory;
import com.beautiflow.global.domain.WeekDay;
import java.time.LocalTime;
import java.util.List;
import lombok.Builder;

public record ShopDetailRes(
    Long id,
    String name,
    String contact,
    String address,
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
            List<TreatmentImageDto> images
    ) {

        @Builder
        public TreatmentDto {}

        public record TreatmentImageDto(Long id, String imageUrl) {

            @Builder
            public TreatmentImageDto {}
        }
    }
}
