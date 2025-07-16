package com.beautiflow.reservation.dto.response;

import com.beautiflow.global.domain.TreatmentCategory;
import com.beautiflow.global.domain.WeekDay;
import java.time.LocalTime;
import java.util.List;
import lombok.Builder;

public record ShopDetailResponse (
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
    public ShopDetailResponse {}

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
            Integer minPrice,
            Integer maxPrice,
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
