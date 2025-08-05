package com.beautiflow.reservation.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.Transient;
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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    private Integer totalDurationMinutes;

    private Integer totalPrice;

	@OneToMany(mappedBy = "reservation")
	private List<ReservationTreatment> reservationTreatments = new ArrayList<>();

	@OneToMany(mappedBy = "reservation")
	private List<ReservationOption> reservationOptions = new ArrayList<>();

	public void updateTotalDurationAndPrice(int duration, int price) {
		this.totalDurationMinutes = duration;
		this.totalPrice = price;
	}

	public void updateSchedule(LocalDate reservationDate, LocalTime startTime, LocalTime endTime, User designer) {
		this.reservationDate = reservationDate;
		this.startTime = startTime;
		this.endTime = endTime;
		this.designer = designer;
	}

	public void clearSchedule() {
		this.reservationDate = null;
		this.startTime = null;
		this.endTime = null;
		this.designer = null;
	}

	@Transient
	private static final ObjectMapper objectMapper = new ObjectMapper();
	public void updateRequestNotes(String requestNotes, List<String> styleImageUrls) {
		this.requestNotes = requestNotes;
		try {
			this.styleImageUrls = styleImageUrls != null ? objectMapper.writeValueAsString(styleImageUrls) : "[]";
		} catch (JsonProcessingException e) {
			throw new RuntimeException("스타일 이미지 URL JSON 변환 실패", e);
		}
	}

	public void clearRequestNotes() {
		this.requestNotes = null;
		this.styleImageUrls = null;
	}

	public void changeStatus(ReservationStatus status) {
		this.status = status;
	}


}

