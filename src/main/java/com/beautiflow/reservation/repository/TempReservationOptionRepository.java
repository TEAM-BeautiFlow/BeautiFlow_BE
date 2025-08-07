package com.beautiflow.reservation.repository;

import com.beautiflow.reservation.domain.Reservation;
import com.beautiflow.reservation.domain.ReservationOption;
import com.beautiflow.reservation.domain.TempReservation;
import com.beautiflow.reservation.domain.TempReservationOption;
import com.beautiflow.reservation.domain.TempReservationTreatment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TempReservationOptionRepository extends JpaRepository<TempReservationOption, Long> {

    List<TempReservationOption> findByTempReservation(TempReservation tempReservation);

    void deleteByTempReservation(TempReservation tempReservation);


}