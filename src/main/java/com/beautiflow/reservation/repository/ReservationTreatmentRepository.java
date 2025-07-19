package com.beautiflow.reservation.repository;

import com.beautiflow.reservation.domain.ReservationTreatment;
import com.beautiflow.reservation.domain.ReservationTreatmentId;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationTreatmentRepository extends
        JpaRepository<ReservationTreatment, ReservationTreatmentId> {
    Optional<ReservationTreatment> findByTreatmentId(Long treatmentId);

}
