package com.beautiflow.reservation.repository;

import com.beautiflow.reservation.domain.Reservation;
import com.beautiflow.reservation.domain.ReservationTreatment;
import com.beautiflow.reservation.domain.ReservationTreatmentId;
import com.beautiflow.reservation.domain.TempReservation;
import com.beautiflow.reservation.domain.TempReservationTreatment;
import com.beautiflow.reservation.domain.TempReservationTreatmentId;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TempReservationTreatmentRepository extends
        JpaRepository<TempReservationTreatment, TempReservationTreatmentId> {
    List<TempReservationTreatment> findByTempReservation(TempReservation tempReservation);
    void deleteByTempReservation(TempReservation tempReservation);


}
