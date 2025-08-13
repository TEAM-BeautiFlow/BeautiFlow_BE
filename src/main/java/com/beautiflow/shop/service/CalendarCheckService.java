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
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.beautiflow.reservation.repository.ReservationRepository.TodayCounts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CalendarCheckService {

  private final ReservationRepository reservationRepository;
  private final ManagedCustomerService managedCustomerService;

  @Transactional(readOnly = true)
  public ReservationMonthRes getTodaySummary(Long designerId) {
    LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
    TodayCounts c = reservationRepository.getTodayCounts(designerId, today);

    long pending   = (c != null && c.getPendingCount()   != null) ? c.getPendingCount()   : 0L;
    long completed = (c != null && c.getCompletedCount() != null) ? c.getCompletedCount() : 0L;
    long cancelled = (c != null && c.getCancelledCount() != null) ? c.getCancelledCount() : 0L;

    return new ReservationMonthRes(pending, completed, cancelled);
  }

  @Transactional(readOnly = true)
  public ReservationDetailRes getReservationDetail(Long id) {
    Reservation reservation = reservationRepository.findFetchAllById(id)
        .orElseThrow(() -> new BeautiFlowException(ReservationErrorCode.RESERVATION_DETAIL_NOT_FOUND));
    return ReservationDetailRes.from(reservation);
  }



  @Transactional
  public UpdateReservationStatusRes updateStatus(Long reservationId, ReservationStatus newStatus) {
    Reservation reservation = reservationRepository.findById(reservationId)
        .orElseThrow(() -> new BeautiFlowException(ReservationErrorCode.RESERVATION_NOT_FOUND));
    reservation.updateStatus(newStatus);
    reservationRepository.save(reservation);

    if (newStatus == ReservationStatus.CONFIRMED) {
      managedCustomerService.autoRegister(
          reservation.getDesigner(),
          reservation.getCustomer()
      );
    }
    return UpdateReservationStatusRes.from(reservation);
  }

  @Transactional(readOnly = true)
  public Page<ReservationListRes> getReservationsByDate(Long designerId, LocalDate date, Pageable pageable) {
    return reservationRepository.findPageByDesignerAndDate(designerId, date, pageable)
        .map(ReservationListRes::from);
  }

  @Transactional(readOnly = true)
  public Page<ReservationListRes> getReservationsByMonth(Long designerId, YearMonth month, Pageable pageable) {
    LocalDate start = month.atDay(1);
    LocalDate end = month.atEndOfMonth();


    return reservationRepository.findPageByDesignerAndMonth(designerId, start, end, pageable)
        .map(ReservationListRes::from);
  }
}