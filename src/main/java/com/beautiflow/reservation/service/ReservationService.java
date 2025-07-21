package com.beautiflow.reservation.service;

import com.beautiflow.global.common.error.OptionErrorCode;
import com.beautiflow.global.common.error.ReservationErrorCode;
import com.beautiflow.global.common.error.ShopErrorCode;
import com.beautiflow.global.common.error.TreatmentErrorCode;
import com.beautiflow.global.common.error.UserErrorCode;
import com.beautiflow.global.common.exception.BeautiFlowException;
import com.beautiflow.global.domain.ApprovalStatus;
import com.beautiflow.global.domain.GlobalRole;
import com.beautiflow.global.domain.PaymentStatus;
import com.beautiflow.global.domain.ReservationStatus;
import com.beautiflow.global.domain.ShopRole;
import com.beautiflow.global.domain.WeekDay;
import com.beautiflow.reservation.domain.Reservation;
import com.beautiflow.reservation.domain.ReservationOption;
import com.beautiflow.reservation.domain.ReservationTreatment;
import com.beautiflow.reservation.domain.ReservationTreatmentId;
import com.beautiflow.reservation.dto.request.TemporaryReservationReq;
import com.beautiflow.reservation.dto.response.TemporaryReservationRes;
import com.beautiflow.reservation.dto.response.AvailableDesignerRes;
import com.beautiflow.reservation.repository.ReservationOptionRepository;
import com.beautiflow.reservation.repository.ReservationRepository;
import com.beautiflow.reservation.repository.ReservationTreatmentRepository;
import com.beautiflow.reservation.repository.ShopMemberRepository;
import com.beautiflow.reservation.repository.TreatmentRepository;
import com.beautiflow.shop.converter.WeekDayConverter;
import com.beautiflow.shop.domain.BusinessHour;
import com.beautiflow.shop.domain.Shop;
import com.beautiflow.shop.domain.ShopMember;
import com.beautiflow.shop.repository.BusinessHourRepository;
import com.beautiflow.shop.repository.ShopRepository;
import com.beautiflow.treatment.domain.OptionItem;
import com.beautiflow.treatment.domain.Treatment;
import com.beautiflow.user.domain.User;
import com.beautiflow.treatment.repository.OptionItemRepository;
import com.beautiflow.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private final ShopRepository shopRepository;
    private final OptionItemRepository optionItemRepository;
    private final UserRepository userRepository;
    private final TreatmentRepository treatmentRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationTreatmentRepository reservationTreatmentRepository;
    private final ReservationOptionRepository reservationOptionRepository;
    private final BusinessHourRepository businessHourRepository;
    private final ShopMemberRepository shopMemberRepository;

    @Transactional
    public Reservation tempSaveReservation(User customer, TemporaryReservationReq request) {
        Shop shop = shopRepository.findById(request.shopId())
                .orElseThrow(() -> new BeautiFlowException(ShopErrorCode.SHOP_NOT_FOUND));
        Treatment treatment = treatmentRepository.findById(request.treatmentId())
                .orElseThrow(() -> new BeautiFlowException(TreatmentErrorCode.TREATMENT_NOT_FOUND));

        // 옵션 아이템 조회
        List<OptionItem> optionItems = request.selectedOptions().stream()
                .map(selectedOption -> optionItemRepository.findById(selectedOption.optionItemId())
                        .orElseThrow(() -> new RuntimeException("OptionItem not found")))
                .collect(Collectors.toList());

        // 총 시간, 총 가격 계산
        int totalDuration = treatment.getDurationMinutes() != null ? treatment.getDurationMinutes() : 0;
        int totalExtraMinutes = optionItems.stream()
                .mapToInt(opt -> opt.getExtraMinutes() != null ? opt.getExtraMinutes() : 0)
                .sum();
        int totalDurationMinutes = totalDuration + totalExtraMinutes;

        int totalPrice = treatment.getPrice() != null ? treatment.getPrice() : 0;
        int totalExtraPrice = optionItems.stream()
                .mapToInt(opt -> opt.getExtraPrice() != null ? opt.getExtraPrice() : 0)
                .sum();
        int totalPriceAmount = totalPrice + totalExtraPrice;

        // Reservation 생성 (빌더에 총 시간/가격 포함)
        Reservation reservation = Reservation.builder()
                .shop(shop)
                .customer(customer)
                .status(ReservationStatus.TEMPORARY)
                .totalDurationMinutes(totalDurationMinutes)
                .totalPrice(totalPriceAmount)
                .build();

        reservationRepository.save(reservation);

        // 시술 연결
        ReservationTreatment resTreatment = ReservationTreatment.builder()
                .id(new ReservationTreatmentId(reservation.getId(), treatment.getId()))
                .reservation(reservation)
                .treatment(treatment)
                .build();
        reservationTreatmentRepository.save(resTreatment);

        // 옵션 연결
        List<ReservationOption> reservationOptions = optionItems.stream()
                .map(optionItem -> ReservationOption.builder()
                        .reservation(reservation)
                        .optionItem(optionItem)
                        .build())
                .collect(Collectors.toList());
        reservationOptionRepository.saveAll(reservationOptions);

        return reservation;
    }

    public Map<LocalDate, Boolean> getAvailableDates(Long shopId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new BeautiFlowException(ShopErrorCode.SHOP_NOT_FOUND));

        Map<LocalDate, Boolean> availableDates = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(30);

        for (LocalDate date = today; !date.isAfter(endDate); date = date.plusDays(1)) {
            boolean isClosed = isShopClosedOnDate(shop, date);
            boolean isFullyBooked = isDateFullyBooked(shop, date);

            boolean isAvailable = !(isClosed || isFullyBooked);
            availableDates.put(date, isAvailable);
        }

        return availableDates;
    }

    private boolean isShopClosedOnDate(Shop shop, LocalDate date) {
        WeekDay weekDay = WeekDayConverter.toWeekDay(date.getDayOfWeek());

        return businessHourRepository.findByShopAndDayOfWeek(shop, weekDay)
                .map(BusinessHour::isClosed)
                .orElse(true);
    }

    private boolean isDateFullyBooked(Shop shop, LocalDate date) {
        List<Reservation> reservations = reservationRepository.findByShopAndReservationDateAndStatus(
                shop, date, ReservationStatus.CONFIRMED);

        Optional<BusinessHour> businessHourOpt = businessHourRepository.findByShopAndDayOfWeek(
                shop, WeekDayConverter.toWeekDay(date.getDayOfWeek()));

        if (businessHourOpt.isEmpty()) return true;

        BusinessHour businessHour = businessHourOpt.get();

        if (businessHour.isClosed()) return true;

        LocalTime open = businessHour.getOpenTime();
        LocalTime close = businessHour.getCloseTime();
        LocalTime breakStart = businessHour.getBreakStart();
        LocalTime breakEnd = businessHour.getBreakEnd();

        List<LocalTime> slots = new ArrayList<>();
        for (LocalTime time = open; time.plusHours(1).isBefore(close.plusSeconds(1)); time = time.plusHours(1)) {
            if (time.isBefore(breakStart) || time.isAfter(breakEnd.minusHours(1))) {
                slots.add(time);
            }
        }

        Set<LocalTime> reservedTimes = reservations.stream()
                .map(Reservation::getStartTime)
                .collect(Collectors.toSet());

        for (LocalTime slot : slots) {
            if (!reservedTimes.contains(slot)) {
                return false;
            }
        }

        return true;
    }

    public Map<String, Boolean> getAvailableTimeSlots(Long shopId, LocalDate date, Long treatmentId, User customer) {

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new BeautiFlowException(ShopErrorCode.SHOP_NOT_FOUND));

        Treatment treatment = treatmentRepository.findById(treatmentId)
                .orElseThrow(() -> new BeautiFlowException(TreatmentErrorCode.TREATMENT_NOT_FOUND));

        // 1) 먼저 임시 예약 조회 (예약과 유저, 샵으로)
        Reservation tempReservation = reservationRepository.findTemporaryByCustomerAndShop(customer, shop)
                .orElseThrow(() -> new BeautiFlowException(ReservationErrorCode.RESERVATION_NOT_FOUND));

        // 2) ReservationTreatmentId 복합키 생성
        ReservationTreatmentId rtId = new ReservationTreatmentId(tempReservation.getId(), treatment.getId());

        // 3) ReservationTreatment 조회
        ReservationTreatment resTreatment = reservationTreatmentRepository.findById(rtId)
                .orElseThrow(() -> new BeautiFlowException(ReservationErrorCode.RESERVATION_NOT_FOUND));

        int treatmentDuration = treatment.getDurationMinutes() != null ? treatment.getDurationMinutes() : 0;
        int optionDuration = tempReservation.getReservationOptions().stream()
                .mapToInt(opt -> opt.getOptionItem().getExtraMinutes() != null ? opt.getOptionItem().getExtraMinutes() : 0)
                .sum();

        int totalMinutes = treatmentDuration + optionDuration;

        WeekDay weekDay = WeekDayConverter.toWeekDay(date.getDayOfWeek());

        BusinessHour bh = businessHourRepository.findByShopAndDayOfWeek(shop, weekDay)
                .orElseThrow(() -> new BeautiFlowException(ShopErrorCode.BUSINESS_HOUR_NOT_FOUND));

        if (bh.isClosed()) {
            return Collections.emptyMap();
        }

        LocalTime open = bh.getOpenTime();
        LocalTime close = bh.getCloseTime();
        LocalTime breakStart = bh.getBreakStart();
        LocalTime breakEnd = bh.getBreakEnd();

        List<Reservation> reservations = reservationRepository.findByShopAndReservationDateAndStatus(
                shop, date, ReservationStatus.CONFIRMED);

        Map<String, Boolean> result = new LinkedHashMap<>();

        for (LocalTime slot = open; !slot.plusMinutes(totalMinutes).isAfter(close); slot = slot.plusMinutes(30)) {
            boolean overlapsBreak = !(slot.plusMinutes(totalMinutes).isBefore(breakStart) || slot.plusMinutes(totalMinutes).equals(breakStart)
                    || slot.isAfter(breakEnd));
            boolean isPast = date.equals(LocalDate.now()) && slot.isBefore(LocalTime.now());

            LocalTime finalSlot = slot;
            boolean conflict = reservations.stream().anyMatch(r -> {
                LocalTime resStart = r.getStartTime();
                LocalTime resEnd = r.getEndTime();
                LocalTime myStart = finalSlot;
                LocalTime myEnd = finalSlot.plusMinutes(totalMinutes);
                return myStart.isBefore(resEnd) && myEnd.isAfter(resStart);
            });

            boolean isAvailable = !(overlapsBreak || isPast || conflict);
            result.put(slot.toString(), isAvailable);
        }

        return result;
    }
    public List<AvailableDesignerRes> getAvailableDesigners(Long shopId, LocalDate date, LocalTime time) {
        // 1. 해당 샵의 승인된 OWNER or DESIGNER만 조회
        List<ShopMember> members = shopMemberRepository.findByShopIdAndStatus(
                shopId,
                ApprovalStatus.APPROVED
        );

        // 2. 멤버별 예약 충돌 검사
        return members.stream()
                .filter(member -> isAvailableAt(member.getUser(), date, time))
                .map(member -> new AvailableDesignerRes(
                        member.getUser().getId(),
                        member.getUser().getName(),
                        member.getProfileImage(),
                        member.getRole() == ShopRole.OWNER,
                        member.getUser().getIntro()
                ))
                .collect(Collectors.toList());
    }

    private boolean isAvailableAt(User user, LocalDate date, LocalTime time) {
        List<Reservation> reservations = reservationRepository
                .findByDesigner_IdAndReservationDateAndStatus(user.getId(), date, ReservationStatus.CONFIRMED);

        // 해당 시간대와 겹치는 예약이 있는지 확인
        return reservations.stream().noneMatch(res -> {
            LocalTime start = res.getStartTime();
            LocalTime end = res.getEndTime();
            return !time.isBefore(start) && time.isBefore(end); // 겹치면 false
        });
    }


}