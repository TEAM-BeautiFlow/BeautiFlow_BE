package com.beautiflow.reservation.service;


import com.beautiflow.global.common.error.ReservationErrorCode;
import com.beautiflow.global.common.exception.BeautiFlowException;
import com.beautiflow.reservation.dto.ReservationMonthRes;
import com.beautiflow.reservation.repository.ReservationRepository;
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

}
