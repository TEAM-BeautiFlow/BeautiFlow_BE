package com.beautiflow.reservation.repository;

import com.beautiflow.reservation.domain.Reservation;
import com.beautiflow.reservation.domain.ReservationOption;
import com.beautiflow.reservation.domain.ReservationTreatment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationOptionRepository extends JpaRepository<ReservationOption, Long> {


}
