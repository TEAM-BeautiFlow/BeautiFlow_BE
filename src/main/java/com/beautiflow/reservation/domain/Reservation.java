package com.beautiflow.reservation.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.beautiflow.global.domain.BaseTimeEntity;
import com.beautiflow.global.domain.PaymentMethod;
import com.beautiflow.global.domain.PaymentStatus;
import com.beautiflow.global.domain.ReservationStatus;
import com.beautiflow.shop.domain.Shop;
import com.beautiflow.user.domain.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "reservations")
public class Reservation extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	private Shop shop;

	@ManyToOne(fetch = FetchType.LAZY)
	private User customer;

	@ManyToOne(fetch = FetchType.LAZY)
	private User designer;

	private LocalDate reservationDate;
	private LocalTime startTime;
	private LocalTime endTime;

	@Enumerated(EnumType.STRING)
	private ReservationStatus status;

	private String requestNotes;

	public void updateStatus(ReservationStatus newStatus) {
		this.status = newStatus;
	}


	@Column(columnDefinition = "json")
	private String styleImageUrls;

	@Enumerated(EnumType.STRING)
	private PaymentMethod paymentMethod;

	@Enumerated(EnumType.STRING)
	private PaymentStatus paymentStatus;

	@OneToMany(mappedBy = "reservation")
	private List<ReservationTreatment> reservationTreatments = new ArrayList<>();

	@OneToMany(mappedBy = "reservation")
	private List<ReservationOption> reservationOptions = new ArrayList<>();
}

