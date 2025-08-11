package com.beautiflow.reservation.domain;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class TempReservationTreatmentId implements Serializable {

    private Long tempReservationId;
    private Long treatmentId;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        TempReservationTreatmentId that = (TempReservationTreatmentId) o;
        return Objects.equals(tempReservationId, that.tempReservationId) &&
                Objects.equals(treatmentId, that.treatmentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tempReservationId, treatmentId);
    }
}