package com.beautiflow.reservation.repository;

import com.beautiflow.global.domain.ReservationStatus;
import com.beautiflow.reservation.domain.Reservation;
import com.beautiflow.reservation.domain.ReservationTreatment;
import com.beautiflow.reservation.domain.TempReservation;
import com.beautiflow.reservation.domain.TempReservationTreatment;
import com.beautiflow.shop.domain.Shop;
import com.beautiflow.user.domain.User;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TempReservationRepository extends JpaRepository<TempReservation, Long> {
    boolean existsByDesigner_IdAndReservationDateAndStartTimeLessThanAndEndTimeGreaterThan(
            Long designerId,
            LocalDate reservationDate,
            LocalTime endTime,
            LocalTime startTime
    );

    Optional<TempReservation> findByCustomerAndShop(User customer, Shop shop);


    Optional<TempReservation> findTemporaryByCustomerAndShop(User customer, Shop shop);


}