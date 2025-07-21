package com.beautiflow.shop.service;


import com.beautiflow.MangedCustomer.dto.CustomerListRes;
import com.beautiflow.MangedCustomer.repository.ManagedCustomerRepository;
import com.beautiflow.MangedCustomer.service.ManagedCustomerService;
import com.beautiflow.global.common.error.ReservationErrorCode;
import com.beautiflow.global.common.exception.BeautiFlowException;
import com.beautiflow.global.domain.ReservationStatus;
import com.beautiflow.reservation.domain.Reservation;
import com.beautiflow.reservation.dto.ReservationDetailRes;
import com.beautiflow.reservation.dto.ReservationListRes;
import com.beautiflow.reservation.dto.ReservationMonthRes;
import com.beautiflow.reservation.dto.TimeSlotRes;
import com.beautiflow.reservation.repository.ReservationRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
  private final ManagedCustomerRepository managedCustomerRepository;
  private final ManagedCustomerService managedCustomerService;





  @Transactional(readOnly = true)
  public List<ReservationMonthRes> getReservedDates(Long designerId, String month) {
    List<ReservationMonthRes> stats = reservationRepository.findReservationStatsByDesignerAndMonth(designerId, month);

    if (stats.isEmpty()) {
      throw new BeautiFlowException(ReservationErrorCode.RESERVATION_NOT_FOUND);
    }

    return stats;
  }

  @Transactional(readOnly = true)
  public List<TimeSlotRes> getReservedTimeSlots(Long designerId, LocalDate date) {
    List<Reservation> reservations = reservationRepository
        .findReservationsWithTreatmentsByDesignerAndDate(designerId, date);

    if (reservations.isEmpty()) {
      throw new BeautiFlowException(ReservationErrorCode.RESERVATION_TIME_NOT_FOUND);
    }

    return reservations.stream()
        .map(reservation -> new TimeSlotRes(
            reservation.getId(),
            reservation.getCustomer().getName(),
            reservation.getStatus(),
            reservation.getStartTime(),
            reservation.getEndTime(),
            reservation.getReservationTreatments().stream()
                .map(rt -> rt.getTreatment().getName())
                .toList()
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

      managedCustomerService.autoRegister(
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
      managedCustomerService.autoRegister(
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


  @Transactional(readOnly = true)
  public List<CustomerListRes> getCustomersByDesigner(Long designerId) {
    return managedCustomerRepository.findByDesignerId(designerId).stream()
        .map(CustomerListRes::from)
        .toList();
  }

  //페이지네이션추가
  public Page<ReservationListRes> getReservationsByDate(Long designerId, LocalDate date, Pageable pageable) {
    return reservationRepository.findPageByDesignerAndDate(designerId, date, pageable)
        .map(ReservationListRes::from);
  }



}