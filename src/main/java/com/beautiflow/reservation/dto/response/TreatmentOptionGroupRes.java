package com.beautiflow.reservation.dto.response;

import java.util.List;

public record TreatmentOptionGroupRes(
        Long id,
        String name,
        Boolean enabled,
        List<TreatmentOptionItemRes> items
) {}