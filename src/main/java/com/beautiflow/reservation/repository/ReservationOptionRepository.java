package com.beautiflow.reservation.repository;

import com.beautiflow.reservation.domain.Reservation;
import com.beautiflow.reservation.domain.ReservationOption;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationOptionRepository extends JpaRepository<ReservationOption, Long> {

}
