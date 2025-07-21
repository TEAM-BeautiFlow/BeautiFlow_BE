package com.beautiflow.reservation.repository;

<<<<<<< HEAD
import com.beautiflow.global.domain.ReservationStatus;
import com.beautiflow.global.domain.WeekDay;
import com.beautiflow.reservation.domain.Reservation;
import com.beautiflow.reservation.domain.ReservationTreatment;
import com.beautiflow.shop.domain.BusinessHour;
import com.beautiflow.shop.domain.Shop;
import com.beautiflow.treatment.domain.Treatment;
import com.beautiflow.user.domain.User;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByShopAndReservationDateAndStatus(Shop shop, LocalDate date, ReservationStatus status);

    Optional<Reservation> findTemporaryByCustomerAndShop(User customer, Shop shop);
    Optional<Reservation> findTemporaryByCustomerAndShopAndReservationTreatments(User customer, Shop shop, ReservationTreatment reservationTreatment);

    List<Reservation> findByDesigner_IdAndReservationDateAndStatus(Long designerId, LocalDate reservationDate, ReservationStatus status);

}

=======
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.beautiflow.global.domain.ReservationStatus;
import com.beautiflow.reservation.domain.Reservation;
import com.beautiflow.user.domain.User;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
	List<Reservation> findByDesignerAndStatus(User designer, ReservationStatus status);


}
>>>>>>> 4f79cbff59b6b78f1f3a3b91732d93e9464b27d2
