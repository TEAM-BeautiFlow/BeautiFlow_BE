package com.beautiflow.reservation.domain;

import java.io.Serializable;

import jakarta.persistence.Embeddable;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ReservationTreatmentId implements Serializable {

	private Long reservationId;
	private Long treatmentId;

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		ReservationTreatmentId that = (ReservationTreatmentId) o;
		return Objects.equals(reservationId, that.reservationId) &&
				Objects.equals(treatmentId, that.treatmentId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(reservationId, treatmentId);
	}
}