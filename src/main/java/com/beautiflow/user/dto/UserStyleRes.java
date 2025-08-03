package com.beautiflow.user.dto;

import com.beautiflow.user.domain.UserStyle;
import java.util.List;
import lombok.Builder;

@Builder
public record UserStyleRes(
        Long styleId,
        Long userId,
        String description,
        List<UserStyleImage> images
) {

    public static UserStyleRes from(UserStyle userStyle) {
        return UserStyleRes.builder()
                .styleId(userStyle.getId())
                .userId(userStyle.getUser().getId())
                .description(userStyle.getDescription())
                .images(
                        userStyle.getImages().stream()
                                .map(UserStyleImage::from)
                                .toList()
                )
                .build();
    }

    @Builder
    public record UserStyleImage(
            Long id,
            String imageUrl,
            String originalFileName,
            String storedFilePath
    ) {
        public static UserStyleImage from(com.beautiflow.user.domain.UserStyleImage img) {
            return UserStyleImage.builder()
                    .id(img.getId())
                    .imageUrl(img.getImageUrl())
                    .originalFileName(img.getOriginalFileName())
                    .storedFilePath(img.getStoredFilePath())
                    .build();
        }
    }
}