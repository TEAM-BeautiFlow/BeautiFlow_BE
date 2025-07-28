package com.beautiflow.reservation.controller;

import com.beautiflow.global.common.ApiResponse;
import com.beautiflow.global.common.error.UserErrorCode;
import com.beautiflow.global.common.exception.BeautiFlowException;
import com.beautiflow.global.common.lock.ReservationLockManager;
import com.beautiflow.global.common.security.CustomOAuth2User;
import com.beautiflow.reservation.domain.Reservation;
import com.beautiflow.reservation.dto.request.UpdateRequestNotesReq;
import com.beautiflow.reservation.dto.response.AvailableDatesRes;
import com.beautiflow.reservation.dto.response.AvailableDesignerRes;
import com.beautiflow.reservation.dto.response.AvailableTimeSlotsRes;
import com.beautiflow.reservation.dto.request.TemporaryReservationReq;
import com.beautiflow.reservation.dto.request.UpdateReservationDateTimeDesignerReq;
import com.beautiflow.reservation.dto.response.MyReservInfoRes;
import com.beautiflow.reservation.service.ReservationService;
import com.beautiflow.shop.domain.Shop;
import com.beautiflow.user.domain.User;
import com.beautiflow.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.hibernate.sql.Update;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Reservation", description = "고객_매장/예약")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/reservations")
@AllArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final UserRepository userRepository;
    private final ReservationLockManager reservationLockManager;

    @Operation(summary = "시술 + 옵션 임시 저장", description = "예약 진행 중 선택한 시술과 옵션을 임시 저장")
    @RequestMapping(
            value = "/shops/{shopId}/temp-save",
            method = {RequestMethod.POST, RequestMethod.PATCH}
    )
    public ResponseEntity<?> tempSaveReservation(
            @PathVariable Long shopId,
            @RequestBody TemporaryReservationReq request,
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User
    ) {
        Long userId = customOAuth2User.getUserId();
        User customer = userRepository.findById(userId)
                .orElseThrow(() -> new BeautiFlowException(UserErrorCode.USER_NOT_FOUND));

        Reservation reservation = reservationService.tempSaveOrUpdateReservation(shopId, customer, request);

        return ResponseEntity.ok("시술과 옵션 임시 저장에 성공했습니다 예약 ID: " + reservation.getId());
    }
    @DeleteMapping("/shops/{shopId}/temp-save")
    @Operation(summary = "임시 예약 삭제", description = "인증된 사용자의 특정 샵 임시 예약 삭제")
    public ResponseEntity<?> deleteTemporaryReservation(
            @PathVariable Long shopId,
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User
    ) {
        Long userId = customOAuth2User.getUserId();
        User customer = userRepository.findById(userId)
                .orElseThrow(() -> new BeautiFlowException(UserErrorCode.USER_NOT_FOUND));

        reservationService.deleteTemporaryReservation(customer, shopId);

        return ResponseEntity.ok("임시 예약이 삭제되었습니다.");
    }


    @PatchMapping("/shops/{shopId}/temp-save/date-time-designer")
    public ResponseEntity<?> updateTempReservationTimeDesigner(
            @PathVariable Long shopId,
            @RequestBody @Valid UpdateReservationDateTimeDesignerReq request,
            @AuthenticationPrincipal CustomOAuth2User principal
    ) {
        Long userId = principal.getUserId();
        User customer = userRepository.findById(userId)
                .orElseThrow(() -> new BeautiFlowException(UserErrorCode.USER_NOT_FOUND));

        reservationService.updateReservationDateTimeAndDesigner(shopId, customer, request.date(), request.time(), request.designerId());

        return ResponseEntity.ok("임시 예약의 날짜, 시간, 디자이너 정보가 성공적으로 업데이트 되었습니다.");
    }

    @DeleteMapping("/shops/{shopId}/unlock")
    @Operation(summary = "임시 예약 락 해제", description = "예약 중단 시 Redisson 락 강제 해제")
    public ResponseEntity<?> unlockReservationLock(
            @PathVariable Long shopId,
            @AuthenticationPrincipal CustomOAuth2User principal
    ) {
        Long userId = principal.getUserId();
        User customer = userRepository.findById(userId)
                .orElseThrow(() -> new BeautiFlowException(UserErrorCode.USER_NOT_FOUND));
        reservationService.unlockTempReservation(shopId, customer);
        return ResponseEntity.ok("시술 날짜, 시간, 디자이너가 unlock 되었습니다.");
    }

    @PatchMapping("/shops/{shopId}/temp-save/req-notes")
    public ResponseEntity<?> updateTempReservationRequestNotes(
            @PathVariable Long shopId,
            @RequestBody @Valid UpdateRequestNotesReq request,
            @AuthenticationPrincipal CustomOAuth2User principal
    ) {
        Long userId = principal.getUserId();
        User customer = userRepository.findById(userId)
                .orElseThrow(() -> new BeautiFlowException(UserErrorCode.USER_NOT_FOUND));

        reservationService.updateReservationRequestNotes(shopId,customer,request);

        return ResponseEntity.ok("임시 예약의 요청 사항과 레퍼런스 이미지 정보가 성공적으로 업데이트 되었습니다.");
    }

    @PatchMapping("/shops/{shopId}/save")
    public ResponseEntity<?> updateTempReservationRequestNotes(
            @PathVariable Long shopId,
            @AuthenticationPrincipal CustomOAuth2User principal
    ) {
        Long userId = principal.getUserId();
        User customer = userRepository.findById(userId)
                .orElseThrow(() -> new BeautiFlowException(UserErrorCode.USER_NOT_FOUND));

        reservationService.saveReservation(shopId, customer);

        return ResponseEntity.ok("예약이 완료되었습니다.");
    }

    @Operation(summary = "30일 이내 예약 가능 날짜 조회", description = "오늘부터 30일 이내 날짜 중, 휴무일/예약 마감 제외하고 예약 가능한 날짜를 반환합니다.")
    @GetMapping("/shops/{shopId}/available-dates")
    public ResponseEntity<ApiResponse<AvailableDatesRes>> getAvailableDates(
            @PathVariable Long shopId
    ) {
        Map<LocalDate, Boolean> availableDates = reservationService.getAvailableDates(shopId);

        return ResponseEntity.ok(ApiResponse.success(new AvailableDatesRes(availableDates)));
    }



    @GetMapping("/shops/{shopId}/available-times")
    public ResponseEntity<ApiResponse<AvailableTimeSlotsRes>> getAvailableTimeSlots(
            @PathVariable Long shopId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam Long treatmentId,
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User
    ) {
        Long userId = customOAuth2User.getUserId();
        User customer = userRepository.findById(userId)
                .orElseThrow(() -> new BeautiFlowException(UserErrorCode.USER_NOT_FOUND));
        Map<String, Boolean> timeSlots = reservationService.getAvailableTimeSlots(shopId, date, treatmentId, customer);
        return ResponseEntity.ok(ApiResponse.success(new AvailableTimeSlotsRes(timeSlots)));
    }

    @GetMapping("/shops/{shopId}/available-designers")
    public ResponseEntity<List<AvailableDesignerRes>> getAvailableDesigners(
            @PathVariable Long shopId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time) {

        List<AvailableDesignerRes> available = reservationService.getAvailableDesigners(shopId, date, time);
        return ResponseEntity.ok(available);
    }

    @GetMapping("/shops/{shopId}/my-reserv-info")
    public ResponseEntity<ApiResponse<MyReservInfoRes>> getMyReservInfo(
            @PathVariable Long shopId,
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User
    ) {
        Long userId = customOAuth2User.getUserId();
        User customer = userRepository.findById(userId)
                .orElseThrow(() -> new BeautiFlowException(UserErrorCode.USER_NOT_FOUND));

        MyReservInfoRes res = reservationService.myReservInfo(shopId, customer);

        return ResponseEntity.ok(ApiResponse.success(res));

    }



}
