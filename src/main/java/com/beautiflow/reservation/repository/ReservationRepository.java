package com.beautiflow.reservation.repository;

import com.beautiflow.global.domain.ReservationStatus;
import com.beautiflow.reservation.domain.Reservation;
import com.beautiflow.reservation.dto.ReservationMonthRes;
import com.beautiflow.shop.domain.Shop;
import com.beautiflow.user.domain.User;
import io.lettuce.core.ScanIterator;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("SELECT new com.beautiflow.reservation.dto.ReservationMonthRes(r.reservationDate, COUNT(r)) " +
            "FROM Reservation r " +
            "WHERE r.designer.id = :designerId " +
            "AND FUNCTION('DATE_FORMAT', r.reservationDate, '%Y-%m') = :month " +
            "GROUP BY r.reservationDate")
    List<ReservationMonthRes> findReservationStatsByDesignerAndMonth(
            @Param("designerId") Long designerId,
            @Param("month") String month
    );

    //시간대별 예약 조회
    // ReservationRepository.java
    @Query("""
    SELECT r FROM Reservation r
    JOIN FETCH r.customer c
    JOIN FETCH r.reservationTreatments rt
    JOIN FETCH rt.treatment t
    WHERE r.designer.id = :designerId
      AND r.reservationDate = :date
""")
    List<Reservation> findReservationsWithTreatmentsByDesignerAndDate(
            @Param("designerId") Long designerId,
            @Param("date") LocalDate date
    );



    @Query("SELECT r FROM Reservation r " + //예약 상세 조회
            "JOIN FETCH r.designer " +
            "JOIN FETCH r.customer " +
            "WHERE r.id = :id")
    Optional<Reservation> findFetchAllById(@Param("id") Long id);

    //페이지네이션추가
    @Query("""
    SELECT r FROM Reservation r
    JOIN FETCH r.customer c
    JOIN FETCH r.reservationTreatments rt
    JOIN FETCH rt.treatment t
    WHERE r.designer.id = :designerId
    AND r.reservationDate = :date
""")
    Page<Reservation> findPageByDesignerAndDate(
            @Param("designerId") Long designerId,
            @Param("date") LocalDate date,
            Pageable pageable
    );

    List<Reservation> findByShopAndReservationDateAndStatus(Shop shop, LocalDate date, ReservationStatus status);

    List<Reservation> findByDesigner_IdAndReservationDateAndStatus(Long designerId, LocalDate reservationDate, ReservationStatus status);

    List<Reservation> findByStatus(ReservationStatus status);

    List<Reservation> findByDesignerAndStatus(User designer, ReservationStatus status);


    @Query("""
    SELECT r FROM Reservation r
    JOIN FETCH r.designer d
    JOIN FETCH r.shop s
    LEFT JOIN FETCH r.reservationOptions ro
    LEFT JOIN FETCH ro.optionItem
    WHERE d.id = :designerId AND r.customer.id = :customerId
  """)
    List<Reservation> findByDesignerIdAndCustomerIdWithAllRelations(Long designerId, Long customerId);


    //고객자동등록  쿼리
    @Query(""" 
        select r from Reservation r
        where r.reservationDate = :targetDate
          and r.endTime <= :cutoff
          and r.status in (
              com.beautiflow.global.domain.ReservationStatus.CONFIRMED
          )
    """)
    List<Reservation> findAutoCompleteTargets(
        @Param("targetDate") LocalDate targetDate,
        @Param("cutoff") LocalTime cutoff
    );
    
    @Query("""
    SELECT DISTINCT r FROM Reservation r
    LEFT JOIN FETCH r.reservationOptions ro
    LEFT JOIN FETCH ro.optionGroup
    WHERE r.status = :status
    """)
    List<Reservation> findAllByStatusWithOptionsAndGroups(@Param("status") ReservationStatus status);
}