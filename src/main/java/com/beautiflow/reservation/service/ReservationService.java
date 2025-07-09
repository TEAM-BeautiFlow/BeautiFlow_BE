package com.beautiflow.reservation.service;

import com.beautiflow.global.common.error.MemberErrorCode;
import com.beautiflow.global.common.exception.BeautiFlowException;
import com.beautiflow.reservation.domain.Reservation;
import com.beautiflow.reservation.dto.response.ReservationDetailResponse;
import com.beautiflow.reservation.dto.response.TimeSlotResponse;
import com.beautiflow.reservation.repository.ReservationRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {

  private final ReservationRepository reservationRepository;

  @Transactional(readOnly = true) //날짜별 예약 유무 캘린더
  public List<LocalDate> getReservedDates(Long designerId, String month) {
    return reservationRepository.findReservedDatesByDesignerAndMonth(designerId, month);
  }

  @Transactional(readOnly = true) //시간대별 예약 현황 조회
  public List<TimeSlotResponse> getReservedTimeSlots(Long designerId, LocalDate date) {
    List<Object[]> results = reservationRepository.findTimeSlotsByDesignerIdAndDate(designerId, date);
    return results.stream()
        .map(row -> new TimeSlotResponse((LocalTime) row[0], (LocalTime) row[1]))
        .toList();
  }



}
