package com.beautiflow.reservation.repository;

import com.beautiflow.reservation.domain.Reservation;
import com.beautiflow.reservation.dto.ReservationMonthRes;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

  @Query("SELECT new com.beautiflow.reservation.dto.ReservationMonthRes(r.reservationDate, true, COUNT(r)) " +
      "FROM Reservation r " +
      "WHERE r.designer.id = :designerId " +
      "AND FUNCTION('DATE_FORMAT', r.reservationDate, '%Y-%m') = :month " +
      "GROUP BY r.reservationDate")
  List<ReservationMonthRes> findReservationStatsByDesignerAndMonth(
      @Param("designerId") Long designerId,
      @Param("month") String month
  );

  //시간대별 예약 조회
  @Query("SELECT r.id, r.customer.name, r.status, r.startTime, r.endTime " +
      "FROM Reservation r " +
      "WHERE r.designer.id = :designerId " +
      "AND r.reservationDate = :date")
  List<Object[]> findTimeSlotsByDesignerIdAndDate(
      @Param("designerId") Long designerId,
      @Param("date") LocalDate date
  );


}
