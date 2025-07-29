package com.beautiflow.reservation.service;

import com.beautiflow.global.common.error.OptionErrorCode;
import com.beautiflow.global.common.error.ReservationErrorCode;
import com.beautiflow.global.common.error.ShopErrorCode;
import com.beautiflow.global.common.error.TreatmentErrorCode;
import com.beautiflow.global.common.exception.BeautiFlowException;
import com.beautiflow.global.common.lock.ReservationLockManager;
import com.beautiflow.global.domain.ApprovalStatus;
import com.beautiflow.global.domain.ReservationStatus;
import com.beautiflow.global.domain.ShopRole;
import com.beautiflow.global.domain.WeekDay;
import com.beautiflow.reservation.domain.Reservation;
import com.beautiflow.reservation.domain.ReservationOption;
import com.beautiflow.reservation.domain.ReservationTreatment;
import com.beautiflow.reservation.domain.ReservationTreatmentId;
import com.beautiflow.reservation.domain.TempReservation;
import com.beautiflow.reservation.domain.TempReservationOption;
import com.beautiflow.reservation.domain.TempReservationTreatment;
import com.beautiflow.reservation.domain.TempReservationTreatmentId;
import com.beautiflow.reservation.dto.request.RequestNotesStyleReq;
import com.beautiflow.reservation.dto.request.TmpReservationReq;
import com.beautiflow.reservation.dto.request.TreatOptionReq;
import com.beautiflow.reservation.dto.response.MyReservInfoRes;
import com.beautiflow.reservation.dto.response.ReservationStatusRes;
import com.beautiflow.reservation.dto.response.ReservationTreatmentInfoRes;
import com.beautiflow.reservation.dto.response.AvailableDesignerRes;
import com.beautiflow.reservation.repository.ReservationOptionRepository;
import com.beautiflow.reservation.repository.ReservationRepository;
import com.beautiflow.reservation.repository.ReservationTreatmentRepository;
import com.beautiflow.reservation.repository.ShopMemberRepository;
import com.beautiflow.reservation.repository.TempReservationOptionRepository;
import com.beautiflow.reservation.repository.TempReservationRepository;
import com.beautiflow.reservation.repository.TempReservationTreatmentRepository;
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
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class ReservationService {
    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);
    private final ShopRepository shopRepository;
    private final OptionItemRepository optionItemRepository;
    private final TreatmentRepository treatmentRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationTreatmentRepository reservationTreatmentRepository;
    private final ReservationOptionRepository reservationOptionRepository;
    private final BusinessHourRepository businessHourRepository;
    private final ShopMemberRepository shopMemberRepository;
    private final ReservationLockManager reservationLockManager;
    private final TempReservationRepository tempReservationRepository;
    private final TempReservationTreatmentRepository tempReservationTreatmentRepository;
    private final TempReservationOptionRepository tempReservationOptionRepository;

    @Transactional
    public void processReservationFlow(Long shopId, User customer, TmpReservationReq request) throws InterruptedException {
        try {
            // 요청 내용에 따른 작업 수행
            if (request.isDeleteTempReservation()) {
                deleteTemporaryReservation(customer, shopId);
                return;
            }
            if (request.tempSaveData() != null) {
                tempSaveOrUpdateReservation(shopId, customer, request.tempSaveData());
            }
            if (request.dateTimeDesignerData() != null) {
                // 이전 락 이름 (예: 이전 임시예약에 저장된 예약정보 기반)
                String previousLockName = getPreviousLockName(shopId, customer);

                // 새로운 락 이름 계산
                String newLockName = calculateLockName(shopId, customer, request);

                // 만약 락 이름이 다르면 기존 락 해제
                if (previousLockName != null && !previousLockName.equals(newLockName)) {
                    reservationLockManager.unlock(previousLockName);
                }
                // 새 락 걸기
                boolean locked = reservationLockManager.tryLock(customer.getId(), newLockName);
                if (!locked) {
                    throw new BeautiFlowException(ReservationErrorCode.RESERVATION_LOCKED);
                }

                updateReservationDateTimeAndDesigner(shopId, customer,
                        request.dateTimeDesignerData().date(),
                        request.dateTimeDesignerData().time(),
                        request.dateTimeDesignerData().designerId());
            }
            if (request.requestNotesStyleData() != null) {
                updateReservationRequestNotes(shopId, customer, request.requestNotesStyleData());
            }
            if (request.isSaveFinalReservation()) {
                saveReservation(shopId, customer);
            }
        } finally {

        }
    }

    @Transactional(readOnly = true)
    public String getPreviousLockName(Long shopId, User customer) {
        // 임시 예약을 찾아서 예약 날짜, 시간, 디자이너 정보를 얻어 락 이름 생성
        TempReservation tempReservation = tempReservationRepository.findByCustomerAndShop(customer,
                        shopRepository.findById(shopId)
                                .orElseThrow(() -> new BeautiFlowException(ShopErrorCode.SHOP_NOT_FOUND)))
                .orElse(null); // 없으면 null 반환

        if (tempReservation == null || tempReservation.getReservationDate() == null
                || tempReservation.getStartTime() == null || tempReservation.getDesigner() == null) {
            return null;
        }

        // 기존 락 이름 생성 (가게+날짜+시간+디자이너)
        return "reservation-lock:" + shopId + ":"
                + tempReservation.getReservationDate().toString() + ":"
                + tempReservation.getStartTime().toString() + ":"
                + tempReservation.getDesigner().getId();
    }

    public String calculateLockName(Long shopId, User customer, TmpReservationReq request) {
        // 기본적으로 기존 예약 정보에서 락 이름을 만들되,
        // 날짜/시간/디자이너 변경 요청이 있으면 그걸 사용하여 새 락 이름 생성

        LocalDate date;
        LocalTime time;
        Long designerId;

        if (request.dateTimeDesignerData() != null) {
            date = request.dateTimeDesignerData().date();
            time = request.dateTimeDesignerData().time();
            designerId = request.dateTimeDesignerData().designerId();
        } else {
            // 날짜/시간/디자이너 변경 요청이 없으면 기존 임시 예약 기준으로
            TempReservation tempReservation = tempReservationRepository.findByCustomerAndShop(customer,
                            shopRepository.findById(shopId)
                                    .orElseThrow(() -> new BeautiFlowException(ShopErrorCode.SHOP_NOT_FOUND)))
                    .orElseThrow(() -> new BeautiFlowException(ReservationErrorCode.TEMP_RESERVATION_NOT_FOUND));

            date = tempReservation.getReservationDate();
            time = tempReservation.getStartTime();
            designerId = tempReservation.getDesigner() != null ? tempReservation.getDesigner().getId() : null;
        }

        if (date == null || time == null || designerId == null) {
            throw new BeautiFlowException(ReservationErrorCode.RESERVATION_MISSING_DATE_TIME_DESIGNER);
        }

        return "reservation-lock:" + shopId + ":"
                + date.toString() + ":"
                + time.toString() + ":"
                + designerId;
    }



    @Transactional
    public TempReservation tempSaveOrUpdateReservation(Long shopId, User customer, TreatOptionReq request) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new BeautiFlowException(ShopErrorCode.SHOP_NOT_FOUND));
        Treatment treatment = treatmentRepository.findById(request.treatmentId())
                .orElseThrow(() -> new BeautiFlowException(TreatmentErrorCode.TREATMENT_NOT_FOUND));

        // 옵션 아이템 조회
        List<OptionItem> optionItems = Optional.ofNullable(request.selectedOptions())
                .orElse(List.of())
                .stream()
                .map(selectedOption -> optionItemRepository.findById(selectedOption.optionItemId())
                        .orElseThrow(() -> new BeautiFlowException(OptionErrorCode.OPTION_ITEM_NOT_FOUND)))
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

        // TEMPORARY 예약 있는지 먼저 조회
        Optional<TempReservation> optional = tempReservationRepository.findByCustomerAndShop(
                customer, shop
        );

        TempReservation tempReservation;
        if(optional.isPresent()) {
            // PATCH: 기존 예약 수정
            tempReservation = optional.get();

            // 기존 시술/옵션 삭제
            tempReservationTreatmentRepository.deleteByTempReservation(tempReservation);
            tempReservationOptionRepository.deleteByTempReservation(tempReservation);

            // 총 시간/가격 업데이트
            tempReservation.updateTotalDurationAndPrice(totalDurationMinutes, totalPriceAmount);
            tempReservation.clearSchedule();
            tempReservation.clearRequestNotes();
        } else {
            // POST: 새 예약 생성
            tempReservation = TempReservation.builder()
                    .shop(shop)
                    .customer(customer)
                    .totalDurationMinutes(totalDurationMinutes)
                    .totalPrice(totalPriceAmount)
                    .build();
            tempReservationRepository.save(tempReservation);
        }

        // 시술 연결
        TempReservationTreatment tempReservationTreatment = TempReservationTreatment.builder()
                .id(new TempReservationTreatmentId(tempReservation.getId(), treatment.getId()))
                .tempReservation(tempReservation)
                .treatment(treatment)
                .build();
        tempReservationTreatmentRepository.save(tempReservationTreatment);

        // 옵션 연결 (옵션이 있을 때만)
        if (!optionItems.isEmpty()) {
            List<TempReservationOption> tempReservationOptions = optionItems.stream()
                    .map(optionItem -> TempReservationOption.builder()
                            .tempReservation(tempReservation)
                            .optionItem(optionItem)
                            .build())
                    .collect(Collectors.toList());
            tempReservationOptionRepository.saveAll(tempReservationOptions);
        }
        return tempReservation;
    }

    @Transactional
    public void deleteTemporaryReservation(User customer, Long shopId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new BeautiFlowException(ShopErrorCode.SHOP_NOT_FOUND));

        TempReservation tempReservation = tempReservationRepository.findByCustomerAndShop(
                customer, shop
        ).orElseThrow(() -> new BeautiFlowException(ReservationErrorCode.TEMP_RESERVATION_NOT_FOUND));

        // 관련된 시술 및 옵션 삭제
        tempReservationTreatmentRepository.deleteByTempReservation(tempReservation);
        tempReservationOptionRepository.deleteByTempReservation(tempReservation);

        // 임시 예약 삭제
        tempReservationRepository.delete(tempReservation);
    }


    @Transactional
    public void updateReservationDateTimeAndDesigner(Long shopId, User customer, LocalDate date, LocalTime time, Long designerId) {
        // 1) 임시 예약 조회
        TempReservation tempReservation = tempReservationRepository
                .findByCustomerAndShop(
                        customer,
                        shopRepository.findById(shopId)
                                .orElseThrow(() -> new BeautiFlowException(ShopErrorCode.SHOP_NOT_FOUND))
                )
                .orElseThrow(() -> new BeautiFlowException(ReservationErrorCode.TEMP_RESERVATION_NOT_FOUND));

        // 2) 디자이너 존재 확인 및 승인 상태 확인
        ShopMember member = shopMemberRepository.findByShopIdAndUserIdAndStatus(shopId, designerId, ApprovalStatus.APPROVED)
                .orElseThrow(() -> new BeautiFlowException(ShopErrorCode.SHOP_MEMBER_NOT_FOUND));

        // 3) 락 이름 정의 (가게 + 날짜+시간+디자이너 기반)
        String lockName = "reservation-lock:" + shopId + ":" + date.toString() + ":" + time.toString() + ":" + designerId;

        try {
            boolean locked = reservationLockManager.tryLock(tempReservation.getId(), lockName);
            if (!locked) {
                throw new BeautiFlowException(ReservationErrorCode.RESERVATION_LOCKED);
            }

            // 4) 중복 예약 체크
            boolean conflictExists = tempReservationRepository
                    .existsByDesigner_IdAndReservationDateAndStartTimeLessThanAndEndTimeGreaterThan(
                            designerId,
                            date,
                            time.plusMinutes(tempReservation.getTotalDurationMinutes()),
                            time
                    );
            if (conflictExists) {
                throw new BeautiFlowException(ReservationErrorCode.RESERVATION_CONFLICT);
            }

            // 5) 예약 업데이트
            tempReservation.updateSchedule(
                    date,
                    time,
                    time.plusMinutes(tempReservation.getTotalDurationMinutes()),
                    member.getUser()
            );
            tempReservation.clearRequestNotes();
            tempReservationRepository.save(tempReservation);

        } catch (InterruptedException e) {
            throw new BeautiFlowException(ReservationErrorCode.RESERVATION_LOCK_INTERRUPTED);
        }
        // 여기선 unlock 안 함 → saveReservation()에서 해제해야 하므로 유지
    }

    @Transactional
    public void unlockTempReservation(Long shopId, User customer) {
        TempReservation tempReservation = tempReservationRepository
                .findByCustomerAndShop(
                        customer,
                        shopRepository.findById(shopId)
                                .orElseThrow(() -> new BeautiFlowException(ShopErrorCode.SHOP_NOT_FOUND))
                )
                .orElseThrow(() -> new BeautiFlowException(ReservationErrorCode.TEMP_RESERVATION_NOT_FOUND));

        String lockName = "reservation-lock:" + shopId + ":" + tempReservation.getReservationDate().toString() + ":" + tempReservation.getStartTime().toString() + ":" + tempReservation.getDesigner();

        try {
            reservationLockManager.unlock(lockName);
            log.info("Unlocked reservation: {}", tempReservation.getId());
        } catch (IllegalStateException e) {
            log.warn("Unlock failed: {}", e.getMessage());
            throw new BeautiFlowException(ReservationErrorCode.UNLOCK_FAILED);
        }

        tempReservation.clearSchedule();
    }

    @Transactional
    public void updateReservationRequestNotes(Long shopId, User customer, RequestNotesStyleReq request) {
        // 1) 임시 예약 조회
        TempReservation tempReservation = tempReservationRepository
                .findByCustomerAndShop(
                        customer,
                        shopRepository.findById(shopId)
                                .orElseThrow(() -> new BeautiFlowException(ShopErrorCode.SHOP_NOT_FOUND))
                )
                .orElseThrow(() -> new BeautiFlowException(ReservationErrorCode.TEMP_RESERVATION_NOT_FOUND));

        // 2) 임시 예약에 요청 사항, 레퍼런스 이미지 정보 업데이트
        tempReservation.updateRequestNotes(request.requestNotes(), request.styleImageUrls());

        tempReservationRepository.save(tempReservation);
    }

    @Transactional
    public void saveReservation(Long shopId, User customer) {
        // 1) Shop 조회
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new BeautiFlowException(ShopErrorCode.SHOP_NOT_FOUND));

        // 2) TempReservation 조회
        TempReservation tempReservation = tempReservationRepository
                .findByCustomerAndShop(customer, shop)
                .orElseThrow(
                        () -> new BeautiFlowException(ReservationErrorCode.TEMP_RESERVATION_NOT_FOUND));

        // 3) Reservation 생성 및 저장
        Reservation reservation = Reservation.builder()
                .shop(shop)
                .customer(customer)
                .designer(tempReservation.getDesigner())
                .reservationDate(tempReservation.getReservationDate())
                .startTime(tempReservation.getStartTime())
                .endTime(tempReservation.getEndTime())
                .status(ReservationStatus.PENDING)
                .requestNotes(tempReservation.getRequestNotes())
                .styleImageUrls(tempReservation.getStyleImageUrls())
                .paymentMethod(tempReservation.getPaymentMethod())
                .paymentStatus(tempReservation.getPaymentStatus())
                .totalDurationMinutes(tempReservation.getTotalDurationMinutes())
                .totalPrice(tempReservation.getTotalPrice())
                .build();

        reservationRepository.save(reservation);

        // 4) TempReservationTreatment -> ReservationTreatment 변환 및 저장
        List<TempReservationTreatment> tempTreatments =
                tempReservationTreatmentRepository.findByTempReservation(tempReservation);

        for (TempReservationTreatment tempTreatment : tempTreatments) {
            ReservationTreatment treatment = ReservationTreatment.builder()
                    .id(new ReservationTreatmentId(reservation.getId(),
                            tempTreatment.getTreatment().getId()))
                    .reservation(reservation)
                    .treatment(tempTreatment.getTreatment())
                    .build();
            reservationTreatmentRepository.save(treatment);
        }

        // 5) TempReservationOption -> ReservationOption 변환 및 저장
        List<TempReservationOption> tempOptions =
                tempReservationOptionRepository.findByTempReservation(tempReservation);

        for (TempReservationOption tempOption : tempOptions) {
            ReservationOption option = ReservationOption.builder()
                    .reservation(reservation)
                    .optionGroup(tempOption.getOptionGroup())
                    .optionItem(tempOption.getOptionItem())
                    .build();
            reservationOptionRepository.save(option);
        }
        String lockName = "reservation-lock:" + shopId + ":" + tempReservation.getReservationDate().toString() + ":" + tempReservation.getStartTime().toString() + ":" + tempReservation.getDesigner();

        // 6) 예약 완료 → Lock 해제
        reservationLockManager.unlock(lockName);

        // 7) 정책에 따라 임시 예약 삭제 (선택)
        tempReservationTreatmentRepository.deleteByTempReservation(tempReservation);
        tempReservationOptionRepository.deleteByTempReservation(tempReservation);
        tempReservationRepository.delete(tempReservation);

    }
    @Transactional(readOnly = true)
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

        if (businessHourOpt.isEmpty()) return false;

        BusinessHour businessHour = businessHourOpt.get();

        if (businessHour.isClosed()) return false;

        LocalTime open = businessHour.getOpenTime();
        LocalTime close = businessHour.getCloseTime();
        LocalTime breakStart = businessHour.getBreakStart();
        LocalTime breakEnd = businessHour.getBreakEnd();

        List<LocalTime> slots = new ArrayList<>();
        for (LocalTime time = open; time.plusHours(1).isBefore(close.plusSeconds(1)); time = time.plusHours(1)) {
            boolean isDuringBreak = false;

            if (breakStart != null && breakEnd != null) {
                isDuringBreak = !time.isBefore(breakStart) && time.isBefore(breakEnd);
            }

            if (!isDuringBreak) {
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

    @Transactional(readOnly = true)
    public Map<String, Boolean> getAvailableTimeSlots(Long shopId, LocalDate date, Long treatmentId, User customer) {

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new BeautiFlowException(ShopErrorCode.SHOP_NOT_FOUND));

        Treatment treatment = treatmentRepository.findById(treatmentId)
                .orElseThrow(() -> new BeautiFlowException(TreatmentErrorCode.TREATMENT_NOT_FOUND));

        // 1) 먼저 임시 예약 조회 (예약과 유저, 샵으로)
        TempReservation tempReservation = tempReservationRepository.findTemporaryByCustomerAndShop(customer, shop)
                .orElseThrow(() -> new BeautiFlowException(ReservationErrorCode.TEMP_RESERVATION_NOT_FOUND));

        // 2) TempReservationTreatmentId 복합키 생성
        TempReservationTreatmentId rtId = new TempReservationTreatmentId(tempReservation.getId(), treatment.getId());

        // 3) ReservationTreatment 조회
        TempReservationTreatment resTreatment = tempReservationTreatmentRepository.findById(rtId)
                .orElseThrow(() -> new BeautiFlowException(ReservationErrorCode.TEMP_RES_TRT_NOT_FOUND));

        int treatmentDuration = treatment.getDurationMinutes() != null ? treatment.getDurationMinutes() : 0;
        int optionDuration = tempReservation.getTempReservationOptions().stream()
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
            boolean overlapsBreak = slot.isBefore(breakEnd) && slot.plusMinutes(totalMinutes).isAfter(breakStart);
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

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    public MyReservInfoRes myReservInfo(Long shopId, User customer) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new BeautiFlowException(ShopErrorCode.SHOP_NOT_FOUND));
        TempReservation tempReservation = tempReservationRepository
                .findByCustomerAndShop(
                        customer,
                        shop
                )
                .orElseThrow(() -> new BeautiFlowException(ReservationErrorCode.TEMP_RESERVATION_NOT_FOUND));
        List<TempReservationTreatment> tempReservationTreatment = tempReservationTreatmentRepository.findByTempReservation(tempReservation);
        if (tempReservationTreatment.isEmpty()) {
            throw new BeautiFlowException(ReservationErrorCode.TEMP_RES_TRT_NOT_FOUND);
        }

        List<TempReservationOption> tempReservationOptions = tempReservationOptionRepository.findByTempReservation(tempReservation);


        return MyReservInfoRes.from(tempReservation, tempReservationTreatment, tempReservationOptions, shop);
    }

    @Transactional(readOnly = true)
    public List<ReservationStatusRes> getReservationsByStatus(ReservationStatus status) {
        List<Reservation> reservations = reservationRepository.findByStatus(status);

        return reservations.stream()
                .map(ReservationStatusRes::from)
                .toList();
    }

    @Transactional
    public void cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BeautiFlowException(ReservationErrorCode.RESERVATION_NOT_FOUND));

        if (reservation.getStatus() == ReservationStatus.PENDING ||
                reservation.getStatus() == ReservationStatus.CONFIRMED) {
            reservation.changeStatus(ReservationStatus.CANCELLED);
        } else {
            throw new BeautiFlowException(ReservationErrorCode.INVALID_CANCEL_STATUS);
        }
    }



}