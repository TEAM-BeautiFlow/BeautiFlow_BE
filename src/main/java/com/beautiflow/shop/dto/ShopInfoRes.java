package com.beautiflow.shop.dto;

import com.beautiflow.shop.domain.Shop;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


public record ShopInfoRes(
    Long shopId,
    List<ImageDto> shopImages,
    String shopName,
    String contact,
    String link,
    String accountInfo,
    String address,
    String introduction
) {
    public record ImageDto(
        Long id,
        String imageUrl
    ) {}

    public static ShopInfoRes from(Shop shop) {
        List<ImageDto> imageDtos = shop.getShopImages() != null ?
            shop.getShopImages().stream()
                .map(image -> new ImageDto(image.getId(), image.getImageUrl()))
                .collect(Collectors.toList()) :
            Collections.emptyList();

        return new ShopInfoRes(
            shop.getId(),
            imageDtos,
            shop.getShopName(),
            shop.getContact(),
            shop.getLink(),
            shop.getAccountInfo(),
            shop.getAddress(),
            shop.getIntroduction()
        );
  }
}