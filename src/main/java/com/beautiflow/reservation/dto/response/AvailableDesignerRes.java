package com.beautiflow.reservation.dto.response;

import java.util.List;

public record AvailableDesignerRes(
        Long id,
        String name,
        String profileImageUrl,
        boolean isOwner,
        String intro
) {}