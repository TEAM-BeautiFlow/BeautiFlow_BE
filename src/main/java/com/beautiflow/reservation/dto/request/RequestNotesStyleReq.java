package com.beautiflow.reservation.dto.request;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public record RequestNotesStyleReq(
        String requestNotes,
        List<MultipartFile> referenceImages
) {}
