package com.beautiflow.user.dto;

import com.beautiflow.user.domain.UserStyle;
import java.util.List;
import lombok.Builder;

@Builder
public record UserStyleRes(
        Long styleId,
        Long userId,
        String description,
        List<String> imageUrls


) {

    public static UserStyleRes from(UserStyle userStyle) {
        return UserStyleRes.builder()
                .userId(userStyle.getUser().getId())
                .description(userStyle.getDescription())
                .imageUrls(
                        userStyle.getImages().stream()
                                .map(image -> image.getImageUrl())
                                .toList()
                )
                .build();
    }

}
