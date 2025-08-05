package com.beautiflow.shop.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ShopExistsRes(
        boolean exists,
        ShopDto shop
) {
    public record ShopDto(
            Long id,
            String name,
            String address,
            String businessRegistrationNumber
    ) {}
}