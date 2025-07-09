package com.beautiflow.reservation.repository;

import com.beautiflow.reservation.domain.Reservation;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

  @Query("SELECT DISTINCT r.reservationDate " + //날짜별 예약 유무 캘린더
      "FROM Reservation r " +
      "WHERE r.designer.id = :designerId " +
      "AND FUNCTION('DATE_FORMAT', r.reservationDate, '%Y-%m') = :month")
  List<LocalDate> findReservedDatesByDesignerAndMonth(
      @Param("designerId") Long designerId,
      @Param("month") String month
  );


  @Query("SELECT r.startTime, r.endTime FROM Reservation r " + // 시간대별 예약 현황 조회
      "WHERE r.designer.id = :designerId AND r.reservationDate = :date")
  List<Object[]> findTimeSlotsByDesignerIdAndDate(@Param("designerId") Long designerId,
      @Param("date") LocalDate date);

  @Query("SELECT r FROM Reservation r " + //예약 상세 조회
      "JOIN FETCH r.designer " +
      "JOIN FETCH r.customer " +
      "WHERE r.id = :id")
  Optional<Reservation> findFetchAllById(@Param("id") Long id);

}
