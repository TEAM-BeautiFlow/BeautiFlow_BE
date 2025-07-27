package com.beautiflow.user.dto;

import java.util.List;

public record UserStylePatchReq(
        String description,
        List<String> deleteImageUrls
) {

}
