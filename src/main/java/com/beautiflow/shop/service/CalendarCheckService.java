package com.beautiflow.shop.service;


import com.beautiflow.customer.dto.CustomerListRes;
import com.beautiflow.customer.repository.DesignerCustomerRepository;
import com.beautiflow.customer.service.DesignerCustomerService;
import com.beautiflow.global.common.error.ReservationErrorCode;
import com.beautiflow.global.common.exception.BeautiFlowException;
import com.beautiflow.global.domain.ReservationStatus;
import com.beautiflow.reservation.domain.Reservation;
import com.beautiflow.reservation.dto.ReservationDetailRes;
import com.beautiflow.reservation.dto.ReservationMonthRes;
import com.beautiflow.reservation.dto.TimeSlotResponse;
import com.beautiflow.reservation.repository.ReservationRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CalendarCheckService {

  private final ReservationRepository reservationRepository;
  private final DesignerCustomerRepository designerCustomerRepository;
  private final DesignerCustomerService designerCustomerService;





  @Transactional(readOnly = true)
  public List<ReservationMonthRes> getReservedDates(Long designerId, String month) {
    List<ReservationMonthRes> stats = reservationRepository.findReservationStatsByDesignerAndMonth(designerId, month);

    if (stats.isEmpty()) {
      throw new BeautiFlowException(ReservationErrorCode.RESERVATION_NOT_FOUND);
    }

    return stats;
  }

  @Transactional(readOnly = true) // 시간대별 예약 현황 조회
  public List<TimeSlotResponse> getReservedTimeSlots(Long designerId, LocalDate date) {
    List<Object[]> results = reservationRepository.findTimeSlotsByDesignerIdAndDate(designerId, date);
    if (results.isEmpty()) {
      throw new BeautiFlowException(ReservationErrorCode.RESERVATION_TIME_NOT_FOUND);
    }
    return results.stream()
        .map(row -> new TimeSlotResponse(
            (Long) row[0],                 // reservationId
            (String) row[1],               // customerName
            row[2].toString(),             // status (enum → string)
            (LocalTime) row[3],            // startTime
            (LocalTime) row[4]             // endTime
        ))
        .toList();
  }

  @Transactional//(readOnly = true) // 고객 상세 정보 조회
  public ReservationDetailRes getReservationDetail(Long id) {
    Reservation reservation = reservationRepository.findFetchAllById(id)
        .orElseThrow(() -> new BeautiFlowException(ReservationErrorCode.RESERVATION_DETAIL_NOT_FOUND));

    // 상태가 CONFIRMED이고, 예약 종료 시간이 지났다면 자동으로 COMPLETED로 변경
    LocalDateTime reservationEnd = LocalDateTime.of(reservation.getReservationDate(), reservation.getEndTime());

    if (reservation.getStatus() == ReservationStatus.CONFIRMED && reservationEnd.isBefore(LocalDateTime.now())) {
      reservation.updateStatus(ReservationStatus.COMPLETED);
      reservationRepository.save(reservation);

      designerCustomerService.autoRegister(
          reservation.getDesigner(),
          reservation.getCustomer(),
          reservation.getShop()
      );

    }

    return ReservationDetailRes.from(reservation);
  }

  @Transactional //예약 상태 변경
  public void updateStatus(Long reservationId, ReservationStatus newStatus) {
    Reservation reservation = reservationRepository.findById(reservationId)
        .orElseThrow(() -> new BeautiFlowException(ReservationErrorCode.RESERVATION_STATUS_NOT_FOUND));

    reservation.updateStatus(newStatus);

    if (newStatus == ReservationStatus.CONFIRMED) {
      designerCustomerService.autoRegister(
          reservation.getDesigner(),
          reservation.getCustomer(),
          reservation.getShop()
      );
    }
  }
  @Transactional(readOnly = true)
  public Reservation getReservationEntity(Long reservationId) {
    return reservationRepository.findById(reservationId)
        .orElseThrow(() -> new BeautiFlowException(ReservationErrorCode.RESERVATION_DETAIL_NOT_FOUND));
  }


  @Transactional
  public List<CustomerListRes> getCustomersByDesigner(Long designerId) {
    return designerCustomerRepository.findByDesignerId(designerId).stream()
        .map(dc -> CustomerListRes.from(dc.getCustomer()))
        .toList();
  }



}