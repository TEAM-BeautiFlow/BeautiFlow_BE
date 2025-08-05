package com.beautiflow.reservation.dto.response;

import java.util.List;

public record ReservationTreatmentInfoRes(
        String treatmentName,
        Integer treatmentPrice,
        List<String> treatmentImageUrls,
        Integer treatmentDurationMinutes
) {}
