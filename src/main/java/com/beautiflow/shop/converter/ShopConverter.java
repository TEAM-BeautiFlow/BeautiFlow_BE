package com.beautiflow.shop.converter;
import com.beautiflow.shop.domain.ShopImage;
import com.beautiflow.shop.domain.ShopMember;
import com.beautiflow.shop.dto.ShopApplyRes;
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
import com.beautiflow.shop.dto.ShopRegistrationRes;
import com.beautiflow.shop.dto.ShopRegistrationRes.ShopMemberRes;
import com.beautiflow.treatment.domain.OptionGroup;
import com.beautiflow.treatment.domain.OptionItem;
import com.beautiflow.treatment.domain.Treatment;
import com.beautiflow.treatment.domain.TreatmentImage;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

public class ShopConverter {

    public static ShopDetailRes toDto(Shop shop) {
        return ShopDetailRes.builder()
                .id(shop.getId())
                .name(shop.getShopName())
                .contact(shop.getContact())
                .address(shop.getAddress())
                .introText(shop.getIntroduction())
                .mainImageUrl(
                        shop.getShopImages()
                                .stream()
                                .findFirst()
                                .map(ShopImage::getImageUrl)
                                .orElse(null)
                )
                .notices(
                        Optional.ofNullable(shop.getNotices())
                                .orElse(Collections.emptyList())
                                .stream()
                                .map(ShopConverter::toNoticeDto)
                                .collect(Collectors.toList())
                )
                .businessHours(
                        Optional.ofNullable(shop.getBusinessHours())
                                .orElse(Collections.emptyList())
                                .stream()
                                .map(ShopConverter::toBusinessHourDto)
                                .collect(Collectors.toList())
                )
                .treatments(
                        Optional.ofNullable(shop.getTreatments())
                                .orElse(Collections.emptyList())
                                .stream()
                                .map(ShopConverter::toTreatmentDto)
                                .collect(Collectors.toList())
                )
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
                .images(
                        Optional.ofNullable(treatment.getImages())
                                .orElse(Collections.emptyList())
                                .stream()
                                .map(ShopConverter::toTreatmentImageDto)
                                .collect(Collectors.toList())
                )
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
                .optionGroups(
                        Optional.ofNullable(treatment.getOptionGroups())
                                .orElse(Collections.emptyList())
                                .stream()
                                .map(ShopConverter::toOptionGroupDto)
                                .collect(Collectors.toList())
                )
                .build();
    }

    private static OptionGroupDto toOptionGroupDto(OptionGroup group) {
        return OptionGroupDto.builder()
                .id(group.getId())
                .name(group.getName())
                .enabled(group.isEnabled())
                .items(
                        Optional.ofNullable(group.getItems())
                                .orElse(Collections.emptyList())
                                .stream()
                                .map(ShopConverter::toOptionItemDto)
                                .collect(Collectors.toList())
                )
                .build();
    }

    private static OptionGroupDto.OptionItemDto toOptionItemDto(OptionItem item) {
        return OptionGroupDto.OptionItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .extraMinutes(item.getExtraMinutes())
                .extraPrice(item.getExtraPrice())
                .description(item.getDescription())
                .build();
    }

    public static ShopRegistrationRes toShopRegistrationRes(Shop shop, ShopMember shopMember) {
        return ShopRegistrationRes.builder()
                .id(shop.getId())
                .name(shop.getShopName())
                .address(shop.getAddress())
                .businessRegistrationNumber(shop.getBusinessRegistrationNumber())
                .shopMember(
                        ShopMemberRes.builder()
                                .id(shopMember.getId())
                                .shopId(shop.getId())
                                .userId(shopMember.getUser().getId())
                                .role(shopMember.getRole())
                                .status(shopMember.getStatus())
                                .appliedAt(shopMember.getAppliedAt())
                                .processedAt(shopMember.getProcessedAt())
                                .build()
                )
                .build();
    }

    public static ShopApplyRes toShopApplyRes(Shop shop, ShopMember shopMember) {
        return ShopApplyRes.builder()
                .shopId(shop.getId())
                .userId(shopMember.getUser().getId())
                .shopMemberId(shopMember.getId())
                .status(shopMember.getStatus())
                .build();
    }
}
