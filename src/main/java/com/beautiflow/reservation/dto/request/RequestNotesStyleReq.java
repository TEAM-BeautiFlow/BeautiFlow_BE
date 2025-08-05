package com.beautiflow.reservation.dto.request;

import java.util.List;

public record RequestNotesStyleReq(
        String requestNotes,
        List<String> styleImageUrls
) {}
