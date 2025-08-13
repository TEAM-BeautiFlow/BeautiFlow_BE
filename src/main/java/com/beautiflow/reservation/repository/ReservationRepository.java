package com.beautiflow.reservation.repository;

import com.beautiflow.global.domain.ReservationStatus;
import com.beautiflow.reservation.domain.Reservation;
import com.beautiflow.shop.domain.Shop;
import com.beautiflow.user.domain.User;
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

    interface TodayCounts {
        Long getPendingCount();
        Long getCompletedCount();
        Long getCancelledCount();
    }

    //시간대별 예약 조회
    // ReservationRepository.java
    @Query("""
    SELECT 
          SUM(CASE WHEN r.status = com.beautiflow.global.domain.ReservationStatus.PENDING THEN 1 ELSE 0 END) as pendingCount,
          SUM(CASE WHEN r.status = com.beautiflow.global.domain.ReservationStatus.COMPLETED THEN 1 ELSE 0 END) as completedCount,
          SUM(CASE WHEN r.status = com.beautiflow.global.domain.ReservationStatus.CANCELLED THEN 1 ELSE 0 END) as cancelledCount
        FROM Reservation r
        WHERE r.designer.id = :designerId
          AND r.reservationDate = :date
    """)
    TodayCounts getTodayCounts(@Param("designerId") Long designerId, @Param("date") LocalDate date);

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



    @Query("""
        SELECT r FROM Reservation r
        JOIN FETCH r.designer
        JOIN FETCH r.customer
        JOIN FETCH r.shop
        WHERE r.id = :id
    """)
    Optional<Reservation> findFetchAllById(@Param("id") Long id);

    @Query("""
    SELECT r FROM Reservation r
        JOIN r.customer c
        WHERE r.designer.id = :designerId
          AND r.reservationDate = :date
    """)
    Page<Reservation> findPageByDesignerAndDate(
        @Param("designerId") Long designerId,
        @Param("date") LocalDate date,
        Pageable pageable
    );

    // ===== 월 단위 페이징: yyyy-MM → [start, end] BETWEEN =====
    @Query("""
        SELECT r FROM Reservation r
        JOIN r.customer c
        WHERE r.designer.id = :designerId
          AND r.reservationDate BETWEEN :startDate AND :endDate
    """)
    Page<Reservation> findPageByDesignerAndMonth(
        @Param("designerId") Long designerId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
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
        SELECT r FROM Reservation r
                WHERE r.reservationDate = :targetDate
                  AND r.endTime <= :cutoff
                  AND r.status IN (com.beautiflow.global.domain.ReservationStatus.CONFIRMED)
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