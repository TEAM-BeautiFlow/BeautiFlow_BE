package com.beautiflow.reservation.service;

import com.beautiflow.global.common.error.OptionErrorCode;
import com.beautiflow.global.common.error.ReservationErrorCode;
import com.beautiflow.global.common.error.ShopErrorCode;
import com.beautiflow.global.common.error.TreatmentErrorCode;
import com.beautiflow.global.common.error.UserErrorCode;
import com.beautiflow.global.common.exception.BeautiFlowException;
import com.beautiflow.global.domain.GlobalRole;
import com.beautiflow.global.domain.PaymentStatus;
import com.beautiflow.global.domain.ReservationStatus;
import com.beautiflow.global.domain.WeekDay;
import com.beautiflow.reservation.domain.Reservation;
import com.beautiflow.reservation.domain.ReservationOption;
import com.beautiflow.reservation.domain.ReservationTreatment;
import com.beautiflow.reservation.dto.request.TemporaryReservationReq;
import com.beautiflow.reservation.dto.response.TemporaryReservationRes;
import com.beautiflow.reservation.dto.response.AvailableDesignerRes;
import com.beautiflow.reservation.repository.DesignerRepository;
import com.beautiflow.reservation.repository.ReservationOptionRepository;
import com.beautiflow.reservation.repository.ReservationRepository;
import com.beautiflow.reservation.repository.ReservationTreatmentRepository;
import com.beautiflow.reservation.repository.TreatmentRepository;
import com.beautiflow.shop.converter.WeekDayConverter;
import com.beautiflow.shop.domain.BusinessHour;
import com.beautiflow.shop.domain.Shop;
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
    private final DesignerRepository designerRepository;

    @Transactional
    public Reservation tempSaveReservation(User customer, TemporaryReservationReq request) {
        Shop shop = shopRepository.findById(request.shopId())
                .orElseThrow(() -> new BeautiFlowException(ShopErrorCode.SHOP_NOT_FOUND));
        Treatment treatment = treatmentRepository.findById(request.treatmentId())
                .orElseThrow(() -> new BeautiFlowException(TreatmentErrorCode.TREATMENT_NOT_FOUND));

        Reservation reservation = Reservation.builder()
                .shop(shop)
                .customer(customer)
                .status(ReservationStatus.TEMPORARY)
                .build();

        reservationRepository.save(reservation);

        // 시술 연결
        ReservationTreatment resTreatment = ReservationTreatment.builder()
                .reservation(reservation)
                .treatment(treatment)
                .build();
        reservationTreatmentRepository.save(resTreatment);

        // 옵션 연결
        List<ReservationOption> reservationOptions = request.selectedOptions().stream()
                .map(selectedOption -> {
                    var optionItem = optionItemRepository.findById(selectedOption.optionItemId())
                            .orElseThrow(() -> new RuntimeException("OptionItem not found"));
                    return ReservationOption.builder()
                            .reservation(reservation)
                            .optionItem(optionItem)
                            .build();
                })
                .collect(Collectors.toList());

        reservationOptionRepository.saveAll(reservationOptions);

        return reservation;
    }
    @Transactional
    public Reservation saveTemporaryReservation(TemporaryReservationReq req, String kakaoId) {
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new BeautiFlowException(UserErrorCode.USER_NOT_FOUND));

        Shop shop = shopRepository.findById(req.shopId())
                .orElseThrow(() -> new BeautiFlowException(ShopErrorCode.SHOP_NOT_FOUND));

        Treatment treatment = treatmentRepository.findById(req.treatmentId())
                .orElseThrow(() -> new BeautiFlowException(TreatmentErrorCode.TREATMENT_NOT_FOUND));

        Reservation reservation = Reservation.builder()
                .shop(shop)
                .customer(user)
                .status(ReservationStatus.TEMPORARY)
                .paymentStatus(PaymentStatus.UNPAID)
                .build();

        reservationRepository.save(reservation);

        ReservationTreatment resTreatment = ReservationTreatment.builder()
                .reservation(reservation)
                .treatment(treatment)
                .build();
        reservationTreatmentRepository.save(resTreatment);

        List<ReservationOption> options = req.selectedOptions().stream()
                .map(optReq -> {
                    OptionItem item = optionItemRepository.findById(optReq.optionItemId())
                            .orElseThrow(() -> new BeautiFlowException(OptionErrorCode.OPTION_ITEM_NOT_FOUND));
                    return ReservationOption.builder()
                            .reservation(reservation)
                            .optionItem(item)
                            .build();
                }).toList();

        reservationOptionRepository.saveAll(options);

        return reservation;
    }


    public TemporaryReservationRes toTemporaryReservationRes(Reservation reservation) {
        List<TemporaryReservationRes.SelectedOptionRes> selectedOptions = reservation.getReservationOptions().stream()
                .map(opt -> {
                    OptionItem item = opt.getOptionItem();
                    return TemporaryReservationRes.SelectedOptionRes.builder()
                            .optionGroupId(item.getOptionGroup().getId())
                            .optionItemId(item.getId())
                            .optionItemName(item.getName())
                            .extraMinutes(item.getExtraMinutes())
                            .extraPrice(item.getExtraPrice())
                            .build();
                }).toList();

        int totalDuration = reservation.getReservationTreatments().stream()
                .mapToInt(rt -> rt.getTreatment().getDurationMinutes() != null ? rt.getTreatment().getDurationMinutes() : 0)
                .sum();
        int totalExtraMinutes = reservation.getReservationOptions().stream()
                .mapToInt(opt -> opt.getOptionItem().getExtraMinutes() != null ? opt.getOptionItem().getExtraMinutes() : 0)
                .sum();
        int durationMinutes = totalDuration + totalExtraMinutes;

        int totalPrice = reservation.getReservationTreatments().stream()
                .mapToInt(rt -> rt.getTreatment().getPrice() != null ? rt.getTreatment().getPrice() : 0)
                .sum();
        int totalExtraPrice = reservation.getReservationOptions().stream()
                .mapToInt(opt -> opt.getOptionItem().getExtraPrice() != null ? opt.getOptionItem().getExtraPrice() : 0)
                .sum();
        int price = totalPrice + totalExtraPrice;

        return TemporaryReservationRes.builder()
                .treatmentId(reservation.getReservationTreatments().get(0).getTreatment().getId())
                .name(reservation.getReservationTreatments().get(0).getTreatment().getName())
                .durationMinutes(durationMinutes)
                .price(price)
                .selectedOptions(selectedOptions)
                .build();
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

    public Map<String, Boolean> getAvailableTimeSlots(Long shopId, LocalDate date, Long treatmentId, String kakaoId) {

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new BeautiFlowException(ShopErrorCode.SHOP_NOT_FOUND));

        Treatment treatment = treatmentRepository.findById(treatmentId)
                .orElseThrow(() -> new BeautiFlowException(TreatmentErrorCode.TREATMENT_NOT_FOUND));
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new BeautiFlowException(UserErrorCode.USER_NOT_FOUND));

        Reservation tempReservation = (Reservation) reservationRepository.findTemporaryByCustomerAndShopAndReservationTreatments(user, shop, treatment)
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
            boolean overlapsBreak = !(slot.plusMinutes(totalMinutes).isBefore(breakStart) || slot.isAfter(breakEnd));
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
    public List<AvailableDesignerRes> getAvailableDesigners(Long shopId) {
        List<User> designers = designerRepository.findByShopMemberships_Shop_IdAndRoles_Id_Role(
                shopId, GlobalRole.STAFF
        );

        return designers.stream()
                .map(user -> new AvailableDesignerRes(
                        user.getId(),
                        user.getName(),
                        user.getStyleImages().isEmpty() ? null : user.getStyleImages().get(0).getImageUrl(), // 예시: 프로필 이미지 (없으면 null)
                        isOwner(user), // 예: 원장 여부 판별 메서드 구현 필요
                        user.getIntro()
                ))
                .collect(Collectors.toList());
    }

    private boolean isOwner(User user) {
        // userRole 중에 원장 역할이 있으면 true (원장 역할 별도 구현에 따라 조정 필요)
        return user.getRoles().stream()
                .anyMatch(role -> role.getRole().equals(GlobalRole.STAFF)); // 예시, 실제 role 이름 확인 필요
    }

}