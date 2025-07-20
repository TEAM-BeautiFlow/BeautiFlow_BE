package com.beautiflow.reservation.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.beautiflow.global.domain.ReservationStatus;
import com.beautiflow.reservation.domain.Reservation;
import com.beautiflow.user.domain.User;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
	List<Reservation> findByDesignerAndStatus(User designer, ReservationStatus status);


}
