package com.beautiflow.shop.service;

import com.beautiflow.MangedCustomer.service.ManagedCustomerService;
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
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CalendarCheckService {

  private final ReservationRepository reservationRepository;
  private final ManagedCustomerService managedCustomerService;

  @Transactional(readOnly = true) //월별 조회
  public List<ReservationMonthRes> getReservedDates(Long designerId, String month) {
    return reservationRepository.findReservationStatsByDesignerAndMonth(designerId, month);
  }

  @Transactional(readOnly = true) // 고객 상세 정보 조회
  public ReservationDetailRes getReservationDetail(Long id) {
    Reservation reservation = reservationRepository.findFetchAllById(id)
        .orElseThrow(() -> new BeautiFlowException(ReservationErrorCode.RESERVATION_DETAIL_NOT_FOUND));
    //존재하는 예약 ID를 조회하는 용도라서 존재해야한 하는게 정상이므로 예외 던짐.

    return ReservationDetailRes.from(reservation);
  }



  @Transactional//예약상태 변경
  public UpdateReservationStatusRes updateStatus(Long reservationId, ReservationStatus newStatus) {
    Reservation reservation = reservationRepository.findById(reservationId)
        .orElseThrow(() -> new BeautiFlowException(ReservationErrorCode.RESERVATION_NOT_FOUND));
    reservation.updateStatus(newStatus);
    reservationRepository.save(reservation); // 상태 반영

    if (newStatus == ReservationStatus.CONFIRMED) {
      managedCustomerService.autoRegister(
          reservation.getDesigner(),
          reservation.getCustomer()
      );
    }
    return UpdateReservationStatusRes.from(reservation);
  }

  //시간대별 조회 페이지네이션추가
  //당일취소 화면 프론트에서 필터링할지, 백엔드에서 필터링할지 의논필요
  @Transactional(readOnly = true)
  public Page<ReservationListRes> getReservationsByDate(Long designerId, LocalDate date, Pageable pageable) {
    return reservationRepository.findPageByDesignerAndDate(designerId, date, pageable)
        .map(ReservationListRes::from);
  }



}