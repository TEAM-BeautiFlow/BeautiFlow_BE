package com.beautiflow.reservation.dto.response;

import com.beautiflow.global.common.error.ReservationErrorCode;
import com.beautiflow.global.common.exception.BeautiFlowException;
import com.beautiflow.reservation.domain.TempReservation;
import com.beautiflow.reservation.domain.TempReservationOption;
import com.beautiflow.reservation.domain.TempReservationTreatment;
import com.beautiflow.shop.domain.Shop;
import com.beautiflow.treatment.domain.Treatment;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
            TempReservation tempReservation,
            List<TempReservationTreatment> tempReservationTreatment,
            List<TempReservationOption> tempReservationOptions,
            Shop shop
    ) {
        if(tempReservationTreatment.isEmpty()) {
            throw new BeautiFlowException(ReservationErrorCode.RESERVATION_TREATMENT_NOT_FOUND);
        }
        TempReservationTreatment rt = tempReservationTreatment.get(0);
        Map<List<String>, Integer> payInfo = new HashMap<>();
        Treatment treatment = rt.getTreatment();
        if (treatment == null) {
            throw new BeautiFlowException(ReservationErrorCode.RESERVATION_TREATMENT_NOT_FOUND);
        }

        String treatmentName = treatment.getName();
        int treatmentPrice = treatment.getPrice();

        if (treatmentName == null) treatmentName = "이름 없음";
        payInfo.put(List.of(treatmentName), treatmentPrice);

        // OptionGroup null-safe
        if (tempReservationOptions != null) {
            for (TempReservationOption option : tempReservationOptions) {
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
                tempReservation.getCustomer().getName(),
                tempReservation.getReservationDate(),
                tempReservation.getStartTime(),
                tempReservation.getTotalDurationMinutes(),
                tempReservation.getShop().getShopName(),
                tempReservation.getDesigner().getName(),
                payInfo,
                shopAccountInfo,
                shop.getDeposit()
        );
    }
}
