package com.beautiflow.reservation.dto.response;

public record TreatmentOptionItemResponse(
        Long id,
        String name,
        Integer extraMinutes,
        String description
) {}