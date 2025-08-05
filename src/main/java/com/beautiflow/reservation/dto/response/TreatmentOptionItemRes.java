package com.beautiflow.reservation.dto.response;

public record TreatmentOptionItemRes(
        Long id,
        String name,
        Integer extraMinutes,
        String description
) {}