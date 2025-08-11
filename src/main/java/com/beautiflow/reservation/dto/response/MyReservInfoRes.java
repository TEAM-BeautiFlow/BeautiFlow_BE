package com.beautiflow.reservation.dto.response;

import com.beautiflow.global.common.error.ReservationErrorCode;
import com.beautiflow.global.common.error.ShopErrorCode;
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
        // 1. 기본 파라미터 null 체크
        if (tempReservation == null) {
            throw new BeautiFlowException(ReservationErrorCode.TEMP_RESERVATION_NOT_FOUND);
        }
        if (shop == null) {
            throw new BeautiFlowException(ShopErrorCode.SHOP_NOT_FOUND);
        }
        if (tempReservationTreatment == null || tempReservationTreatment.isEmpty()) {
            throw new BeautiFlowException(ReservationErrorCode.RESERVATION_TREATMENT_NOT_FOUND);
        }

        TempReservationTreatment rt = tempReservationTreatment.get(0);
        Map<List<String>, Integer> payInfo = new HashMap<>();

        // 2. Treatment null 체크
        Treatment treatment = rt.getTreatment();
        if (treatment == null) {
            throw new BeautiFlowException(ReservationErrorCode.RESERVATION_TREATMENT_NOT_FOUND);
        }

        String treatmentName = treatment.getName() != null ? treatment.getName() : "이름 없음";
        int treatmentPrice = treatment.getPrice() != null ? treatment.getPrice() : 0;

        payInfo.put(List.of(treatmentName), treatmentPrice);

        // 3. OptionGroup null-safe
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

        // 4. Shop 계좌 정보 null-safe
        Map<String, String> shopAccountInfo = new LinkedHashMap<>();
        shopAccountInfo.put("bankName", shop.getBankName() != null ? shop.getBankName() : "");
        shopAccountInfo.put("accountNumber", shop.getAccountNumber() != null ? shop.getAccountNumber() : "");
        shopAccountInfo.put("accountHolder", shop.getAccountHolder() != null ? shop.getAccountHolder() : "");

        // 5. TempReservation 필드들 null-safe 처리
        String customerName = "";
        if (tempReservation.getCustomer() != null && tempReservation.getCustomer().getName() != null) {
            customerName = tempReservation.getCustomer().getName();
        }

        String shopName = "";
        if (tempReservation.getShop() != null && tempReservation.getShop().getShopName() != null) {
            shopName = tempReservation.getShop().getShopName();
        }

        String designerName = "";
        if (tempReservation.getDesigner() != null && tempReservation.getDesigner().getName() != null) {
            designerName = tempReservation.getDesigner().getName();
        }

        Integer totalDurationMinutes = tempReservation.getTotalDurationMinutes() != null ?
                tempReservation.getTotalDurationMinutes() : 0;

        Integer deposit = shop.getDeposit() != null ? shop.getDeposit() : 0;

        return new MyReservInfoRes(
                customerName,
                tempReservation.getReservationDate(), // null 가능 (비즈니스 로직상 허용)
                tempReservation.getStartTime(),       // null 가능 (비즈니스 로직상 허용)
                totalDurationMinutes,
                shopName,
                designerName,
                payInfo,
                shopAccountInfo,
                deposit
        );
    }
}
