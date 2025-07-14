package com.beautiflow.shop.dto;

import java.util.List;

public record ShopUpdateReq(
    String shopName,
    String contact,
    String link,
    String accountInfo,
    String address,
    String introduction,
    List<Long> deleteImageIds
) {
}