package com.beautiflow.shop.converter;

import com.beautiflow.shop.dto.ShopDetailRes;
import com.beautiflow.shop.dto.ShopDetailRes.BusinessHourDto;
import com.beautiflow.shop.dto.ShopDetailRes.NoticeDto;
import com.beautiflow.shop.dto.ShopDetailRes.TreatmentDto;
import com.beautiflow.reservation.dto.response.TreatmentDetailWithOptionRes;
import com.beautiflow.reservation.dto.response.TreatmentDetailWithOptionRes.OptionGroupDto;
import com.beautiflow.reservation.dto.response.TreatmentDetailWithOptionRes.TreatmentImageDto;
import com.beautiflow.shop.domain.BusinessHour;
import com.beautiflow.shop.domain.Shop;
import com.beautiflow.shop.domain.ShopNotice;
import com.beautiflow.treatment.domain.OptionGroup;
import com.beautiflow.treatment.domain.OptionItem;
import com.beautiflow.treatment.domain.Treatment;
import com.beautiflow.treatment.domain.TreatmentImage;
import java.util.stream.Collectors;

public class ShopConverter {

    public static ShopDetailRes toDto(Shop shop) {
        return ShopDetailRes.builder()
                .id(shop.getId())
                .name(shop.getShopName())
                .contact(shop.getContact())
                .location(shop.getAddress())
                .introText(shop.getIntroduction())
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
                .price(treatment.getPrice())
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

    public static TreatmentDetailWithOptionRes toTreatmentDetailWithOptionResponse(Treatment treatment) {
        return TreatmentDetailWithOptionRes.builder()
                .id(treatment.getId())
                .name(treatment.getName())
                .durationMinutes(treatment.getDurationMinutes())
                .price(treatment.getPrice())
                .description(treatment.getDescription())
                .images(treatment.getImages().stream()
                        .map(image -> TreatmentImageDto.builder()
                                .id(image.getId())
                                .imageUrl(image.getImageUrl())
                                .build())
                        .collect(Collectors.toList()))
                .optionGroups(treatment.getOptionGroups().stream()
                        .map(ShopConverter::toOptionGroupDto)
                        .collect(Collectors.toList()))
                .build();
    }

    private static OptionGroupDto toOptionGroupDto(OptionGroup group) {
        return OptionGroupDto.builder()
                .id(group.getId())
                .name(group.getName())
                .enabled(group.isEnabled())
                .items(group.getItems().stream()
                        .map(ShopConverter::toOptionItemDto)
                        .collect(Collectors.toList()))
                .build();
    }

    private static OptionGroupDto.OptionItemDto toOptionItemDto(OptionItem item) {
        return OptionGroupDto.OptionItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .extraMinutes(item.getExtraMinutes())
                .description(item.getDescription())
                .build();
    }
}
