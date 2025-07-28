package com.beautiflow.reservation.dto.response;

import com.beautiflow.global.common.error.ReservationErrorCode;
import com.beautiflow.global.common.exception.BeautiFlowException;
import com.beautiflow.reservation.domain.Reservation;
import com.beautiflow.reservation.domain.ReservationOption;
import com.beautiflow.reservation.domain.ReservationTreatment;
import com.beautiflow.shop.domain.Shop;
import com.beautiflow.treatment.domain.OptionGroup;
import com.beautiflow.treatment.domain.OptionItem;
import com.beautiflow.treatment.domain.Treatment;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public record MyReservInfoRes(
        String customerName,
        LocalDate reservationDate,
        LocalTime startTime,
        Integer durationMinutes,
        String shopName,
        String designerName,

        Map<List<String>, Integer> payInfo,
        Map<String, String> shopAccountInfo,
        Integer deposit


) {

    public static MyReservInfoRes from(
            Reservation reservation,
            Optional<ReservationTreatment> reservationTreatment,
            List<ReservationOption> reservationOptions,
            Shop shop
    ) {
        if(reservationTreatment.isEmpty()) {
            throw new BeautiFlowException(ReservationErrorCode.RESERVATION_TREATMENT_NOT_FOUND);
        }
        Map<List<String>, Integer> payInfo = new HashMap<>();
        ReservationTreatment rt = reservationTreatment.orElseThrow(() ->
                new BeautiFlowException(ReservationErrorCode.RESERVATION_TREATMENT_NOT_FOUND));

        Treatment treatment = rt.getTreatment();
        if (treatment == null) {
            throw new BeautiFlowException(ReservationErrorCode.RESERVATION_TREATMENT_NOT_FOUND);
        }

        String treatmentName = treatment.getName();
        int treatmentPrice = treatment.getPrice();

        if (treatmentName == null) treatmentName = "이름 없음";
        payInfo.put(List.of(treatmentName), treatmentPrice);

        // OptionGroup null-safe
        if (reservationOptions != null) {
            for (ReservationOption option : reservationOptions) {
                if (option == null) continue;

                String groupName = "옵션그룹 없음";
                String itemName = "옵션이름 없음";
                Integer extraPrice = 0;

                if (option.getOptionGroup() != null && option.getOptionGroup().getName() != null) {
                    groupName = option.getOptionGroup().getName();
                }

                if (option.getOptionItem() != null) {
                    if (option.getOptionItem().getName() != null) {
                        itemName = option.getOptionItem().getName();
                    }
                    if (option.getOptionItem().getExtraPrice() != null) {
                        extraPrice = option.getOptionItem().getExtraPrice();
                    }
                }

                payInfo.put(List.of(groupName, itemName), extraPrice);
            }
        }


        Map<String, String> shopAccountInfo = new LinkedHashMap<>();
        shopAccountInfo.put("bankName", shop.getBankName());
        shopAccountInfo.put("accountNumber", shop.getAccountNumber());
        shopAccountInfo.put("accountHolder", shop.getAccountHolder());

        return new MyReservInfoRes(
                reservation.getCustomer().getName(),
                reservation.getReservationDate(),
                reservation.getStartTime(),
                reservation.getTotalDurationMinutes(),
                reservation.getShop().getShopName(),
                reservation.getDesigner().getName(),
                payInfo,
                shopAccountInfo,
                shop.getDeposit()
        );
    }
}
