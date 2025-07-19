package com.beautiflow.shop.service;


import com.beautiflow.global.common.error.ReservationErrorCode;
import com.beautiflow.global.common.exception.BeautiFlowException;
import com.beautiflow.reservation.dto.ReservationMonthRes;
import com.beautiflow.reservation.dto.TimeSlotResponse;
import com.beautiflow.reservation.repository.ReservationRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {

  private final ReservationRepository reservationRepository;

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




}
