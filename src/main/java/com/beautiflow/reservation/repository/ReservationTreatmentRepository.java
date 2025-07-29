package com.beautiflow.reservation.repository;

import com.beautiflow.reservation.domain.Reservation;
import com.beautiflow.reservation.domain.ReservationTreatment;
import com.beautiflow.reservation.domain.ReservationTreatmentId;
import com.beautiflow.treatment.domain.Treatment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationTreatmentRepository extends
        JpaRepository<ReservationTreatment, ReservationTreatmentId> {

}
