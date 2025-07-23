package com.beautiflow.reservation.dto.request;

import java.util.List;
import lombok.Builder;

public record UpdateRequestNotesReq(
    String requestNotes,
    List<String> styleImageUrls
) {
    @Builder
    public UpdateRequestNotesReq {}
}
