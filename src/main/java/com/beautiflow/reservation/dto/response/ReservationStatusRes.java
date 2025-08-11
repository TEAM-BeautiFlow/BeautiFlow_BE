package com.beautiflow.reservation.dto.response;

import com.beautiflow.global.domain.ReservationStatus;
import com.beautiflow.reservation.domain.Reservation;
import com.beautiflow.treatment.domain.Treatment;
import com.beautiflow.treatment.domain.TreatmentImage;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;


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
                reservation.getRequestNotes(),
                reservation.getStyleImageUrls()
        );
    }
}