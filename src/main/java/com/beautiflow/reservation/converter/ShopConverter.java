package com.beautiflow.reservation.converter;

import com.beautiflow.reservation.dto.response.ShopDetailResponse;
import com.beautiflow.reservation.dto.response.ShopDetailResponse.BusinessHourDto;
import com.beautiflow.reservation.dto.response.ShopDetailResponse.NoticeDto;
import com.beautiflow.reservation.dto.response.ShopDetailResponse.TreatmentDto;
import com.beautiflow.shop.domain.BusinessHour;
import com.beautiflow.shop.domain.Shop;
import com.beautiflow.shop.domain.ShopNotice;
import com.beautiflow.treatment.domain.Treatment;
import com.beautiflow.treatment.domain.TreatmentImage;
import java.util.stream.Collectors;

public class ShopConverter {

    public static ShopDetailResponse toDto(Shop shop) {
        return ShopDetailResponse.builder()
                .id(shop.getId())
                .name(shop.getName())
                .contact(shop.getContact())
                .location(shop.getLocation())
                .introText(shop.getIntroText())
                .mainImageUrl(shop.getMainImageUrl())
                .notices(shop.getNotices().stream()
                        .map(ShopConverter::toNoticeDto)
                        .collect(Collectors.toList()))
                .notices(shop.getNotices().stream()
                        .map(ShopConverter::toNoticeDto)
                        .collect(Collectors.toList()))
                .businessHours(shop.getBusinessHours().stream()
                        .map(ShopConverter::toBusinessHourDto)
                        .collect(Collectors.toList()))
                .treatments(shop.getTreatments().stream()
                        .map(ShopConverter::toTreatmentDto)
                        .collect(Collectors.toList()))
                .build();
    }
    private static NoticeDto toNoticeDto(ShopNotice notice) {
        return NoticeDto.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .build();
    }

    private static BusinessHourDto toBusinessHourDto(BusinessHour businessHour) {
        return BusinessHourDto.builder()
                .dayOfWeek(businessHour.getDayOfWeek())
                .openTime(businessHour.getOpenTime())
                .closeTime(businessHour.getCloseTime())
                .breakStart(businessHour.getBreakStart())
                .breakEnd(businessHour.getBreakEnd())
                .isClosed(businessHour.isClosed())
                .build();
    }

    private static TreatmentDto toTreatmentDto(Treatment treatment) {
        return TreatmentDto.builder()
                .id(treatment.getId())
                .category(treatment.getCategory())
                .name(treatment.getName())
                .minPrice(treatment.getMinPrice())
                .maxPrice(treatment.getMaxPrice())
                .durationMinutes(treatment.getDurationMinutes())
                .description(treatment.getDescription())
                .images(treatment.getImages().stream()
                        .map(ShopConverter::toTreatmentImageDto)
                        .collect(Collectors.toList()))
                .build();
    }

    private static TreatmentDto.TreatmentImageDto toTreatmentImageDto(TreatmentImage image) {
        return TreatmentDto.TreatmentImageDto.builder()
                .id(image.getId())
                .imageUrl(image.getImageUrl())
                .build();
    }
}
