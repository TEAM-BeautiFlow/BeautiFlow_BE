package com.beautiflow.reservation.dto.response;

import java.util.List;

public record TreatmentOptionGroupResponse(
        Long id,
        String name,
        Boolean enabled,
        List<TreatmentOptionItemResponse> items
) {}