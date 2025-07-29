package com.beautiflow.reservation.dto.request;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.Getter;

public record TmpReservationReq(
        boolean deleteTempReservation,
        TreatOptionReq tempSaveData,
        DateTimeDesignerReq dateTimeDesignerData,
        RequestNotesStyleReq requestNotesStyleData,
        boolean saveFinalReservation
) {

    public boolean isDeleteTempReservation() {
        return deleteTempReservation;
    }

    public boolean isSaveFinalReservation() {
        return saveFinalReservation;
    }
}