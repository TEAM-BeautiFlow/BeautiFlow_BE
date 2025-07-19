package com.beautiflow.reservation.repository;

import com.beautiflow.reservation.domain.Reservation;
import com.beautiflow.reservation.dto.ReservationMonthRes;
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

}
