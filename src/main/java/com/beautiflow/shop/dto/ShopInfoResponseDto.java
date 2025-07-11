package com.beautiflow.shop.dto;

import java.util.List;


public record ShopInfoResponseDto(
    Long shopId,
    List<String> shopImageUrls,
    String shopName,
    String contact,
    String link,
    String accountInfo,
    String address,
    String introduction
) {
}
