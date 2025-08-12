package com.beautiflow.reservation.dto.response;

import java.util.List;

public record ReservationOptionGroupRes(
        String groupName,
        List<ReservationOptionItemRes> optionItems
) {}
