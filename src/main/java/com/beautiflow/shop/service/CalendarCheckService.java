package com.beautiflow.shop.service;

import com.beautiflow.ManagedCustomer.service.ManagedCustomerService;
import com.beautiflow.global.common.error.ReservationErrorCode;
import com.beautiflow.global.common.exception.BeautiFlowException;
import com.beautiflow.global.domain.ReservationStatus;
import com.beautiflow.reservation.domain.Reservation;
import com.beautiflow.reservation.dto.ReservationDetailRes;
import com.beautiflow.reservation.dto.ReservationListRes;
import com.beautiflow.reservation.dto.ReservationMonthRes;
import com.beautiflow.reservation.dto.UpdateReservationStatusRes;
import com.beautiflow.reservation.repository.ReservationRepository;
import com.beautiflow.reservation.repository.ReservationRepository.TodayCounts;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CalendarCheckService {

  private final ReservationRepository reservationRepository;
  private final ManagedCustomerService managedCustomerService;

  /**
   * 당일 요약: PENDING / COMPLETED / CANCELLED
   * DTO는 기존 ReservationMonthRes(pending, completed, cancelled) 재사용
   */
  @Transactional(readOnly = true)
  public ReservationMonthRes getTodaySummary(Long designerId) {
    LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
    TodayCounts c = reservationRepository.getTodayCounts(designerId, today);

    long pending   = (c != null && c.getPendingCount()   != null) ? c.getPendingCount()   : 0L;
    long completed = (c != null && c.getCompletedCount() != null) ? c.getCompletedCount() : 0L;
    long cancelled = (c != null && c.getCancelledCount() != null) ? c.getCancelledCount() : 0L;

    return new ReservationMonthRes(pending, completed, cancelled);
  }

  /** 예약 상세 조회 */
  @Transactional(readOnly = true)
  public ReservationDetailRes getReservationDetail(Long id) {
    Reservation reservation = reservationRepository.findFetchAllById(id)
        .orElseThrow(() -> new BeautiFlowException(ReservationErrorCode.RESERVATION_DETAIL_NOT_FOUND));
    return ReservationDetailRes.from(reservation);
  }

  /** 예약 상태 변경 */
  @Transactional
  public UpdateReservationStatusRes updateStatus(Long reservationId, ReservationStatus newStatus) {
    Reservation reservation = reservationRepository.findById(reservationId)
        .orElseThrow(() -> new BeautiFlowException(ReservationErrorCode.RESERVATION_NOT_FOUND));

    reservation.updateStatus(newStatus);
    reservationRepository.save(reservation);

    if (newStatus == ReservationStatus.CONFIRMED) {
      // 니 프로젝트 시그니처에 맞춰 두 파라미터 버전 유지
      managedCustomerService.autoRegister(
          reservation.getDesigner(),
          reservation.getCustomer()
      );
    }
    return UpdateReservationStatusRes.from(reservation);
  }

  //(기존) 특정 날짜 예약 리스트 조회 (페이징)
  @Transactional(readOnly = true)
  public Page<ReservationListRes> getReservationsByDate(Long designerId, LocalDate date, Pageable pageable) {
    return reservationRepository.findPageByDesignerAndDate(designerId, date, pageable)
        .map(ReservationListRes::from);
  }

 //신규 특정 날짜 예약 리스트 조회 (페이징)
  @Transactional(readOnly = true)
  public Page<ReservationListRes> getReservationsByMonth(Long designerId, YearMonth month, Pageable pageable) {
    LocalDate start = month.atDay(1);
    LocalDate end = month.atEndOfMonth();

    return reservationRepository.findPageByDesignerAndMonth(designerId, start, end, pageable)
        .map(ReservationListRes::from);
  }
}
