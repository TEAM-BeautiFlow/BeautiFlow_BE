package com.beautiflow.reservation.domain;

import com.beautiflow.treatment.domain.Treatment;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
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
@Table(name = "reservation_treatments")
public class ReservationTreatment {
	@EmbeddedId
	private ReservationTreatmentId id;

	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("reservationId")
	private Reservation reservation;

	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("treatmentId")
	private Treatment treatment;
}
