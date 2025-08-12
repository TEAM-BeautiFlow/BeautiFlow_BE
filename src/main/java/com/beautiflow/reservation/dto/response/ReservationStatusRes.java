package com.beautiflow.reservation.dto.response;

import com.beautiflow.global.domain.ReservationStatus;
import com.beautiflow.reservation.domain.Reservation;
import com.beautiflow.reservation.domain.ReservationOption;
import com.beautiflow.reservation.domain.TempReservationOption;
import com.beautiflow.treatment.domain.Treatment;
import com.beautiflow.treatment.domain.TreatmentImage;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public record ReservationStatusRes(
        ReservationStatus status,
        Long reservationId,
        Long shopId,
        String shopName,
        String shopAddress,
        LocalDate reservationDate,
        LocalTime startTime,
        Integer totalDurationMinutes,
        String customerName,
        List<ReservationTreatmentInfoRes> treatments,
        List<ReservationOptionGroupRes> optionGroups,
        String requestNotes,
        String styleImageUrls
) {
    public static ReservationStatusRes from(Reservation reservation) {
        List<ReservationTreatmentInfoRes> treatmentInfos = reservation.getReservationTreatments().stream()
                .map(rt -> {
                    Treatment t = rt.getTreatment();
                    List<String> imageUrls = t.getImages().stream()
                            .map(TreatmentImage::getImageUrl)
                            .toList();
                    return new ReservationTreatmentInfoRes(
                            t.getName(),
                            t.getPrice(),
                            imageUrls,
                            t.getDurationMinutes()
                    );
                })
                .toList();

        // 옵션 그룹별로 Map으로 묶기
        Map<String, List<ReservationOptionItemRes>> groupedOptions = new LinkedHashMap<>();

        List<ReservationOption> reservationOptions = reservation.getReservationOptions();
        if (reservationOptions != null) {
            for (ReservationOption option : reservationOptions) {
                if (option == null) continue;

                String groupName = "옵션그룹 없음";
                if (option.getOptionGroup() != null && option.getOptionGroup().getName() != null) {
                    groupName = option.getOptionGroup().getName();
                }

                String itemName = "옵션이름 없음";
                if (option.getOptionItem() != null) {
                    if (option.getOptionItem().getName() != null) {
                        itemName = option.getOptionItem().getName();
                    }
                }

                groupedOptions.computeIfAbsent(groupName, k -> new ArrayList<>())
                        .add(new ReservationOptionItemRes(itemName));
            }
        }

        // Map을 List<ReservationOptionGroupRes>로 변환
        List<ReservationOptionGroupRes> optionGroupResList = groupedOptions.entrySet().stream()
                .map(e -> new ReservationOptionGroupRes(e.getKey(), e.getValue()))
                .toList();

        return new ReservationStatusRes(
                reservation.getStatus(),
                reservation.getId(),
                reservation.getShop().getId(),
                reservation.getShop().getShopName(),
                reservation.getShop().getAddress(),
                reservation.getReservationDate(),
                reservation.getStartTime(),
                reservation.getTotalDurationMinutes(),
                reservation.getCustomer().getName(),
                treatmentInfos,
                optionGroupResList,
                reservation.getRequestNotes(),
                reservation.getStyleImageUrls()
        );
    }
}