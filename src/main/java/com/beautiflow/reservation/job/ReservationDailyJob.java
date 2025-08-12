package com.beautiflow.reservation.job;

import com.beautiflow.global.domain.ReservationStatus;
import com.beautiflow.reservation.domain.Reservation;
import com.beautiflow.reservation.repository.ReservationRepository;
import com.beautiflow.ManagedCustomer.service.ManagedCustomerService;
import java.time.*;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservationDailyJob {

  private final ReservationRepository reservationRepository;
  private final ManagedCustomerService managedCustomerService;

  // 매일 00:05(KST) 실행 - 정각 직후 유예 5분
  @Scheduled(cron = "0 5 0 * * *", zone = "Asia/Seoul")
  @Transactional
  public void completeYesterdayReservations() {
    ZoneId zone = ZoneId.of("Asia/Seoul");
    LocalDate today = LocalDate.now(zone);
    LocalDate target = today.minusDays(1);

    // 어제 날짜 & endTime이 하루 끝(23:59:59) 이전인 예약들
    LocalTime cutoff = LocalTime.of(23, 59, 59);

    List<Reservation> targets = reservationRepository.findAutoCompleteTargets(target, cutoff);

    for (Reservation r : targets) {
      // 취소/노쇼는 제외 (쿼리에서 이미 제외되지만 안전빵 방어)
      if (r.getStatus() == ReservationStatus.CANCELLED || r.getStatus() == ReservationStatus.NO_SHOW) {
        continue;
      }

      // 상태 변경
      r.changeStatus(ReservationStatus.COMPLETED);

      // 고객 자동 등록 (중복 방지는 서비스 내부 exists 체크로 처리)
      managedCustomerService.autoRegister(r.getDesigner(), r.getCustomer(), r.getShop());
    }
  }
}
