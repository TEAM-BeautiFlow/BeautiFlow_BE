package com.beautiflow.user.dto;

import lombok.Builder;

@Builder
public record UserRes(
        Long id,
        String name,
        String contact
) {

}
