package com.beautiflow.shop.dto;

import java.util.List;

public record ShopUpdateReq(
    String shopName,
    String contact,
    String link,
    String accountInfo,
    String address,
    String introduction,
    String bankName,
    String accountNumber,
    String accountHolder,
    Integer depositAmount,
    List<Long> deleteImageIds
) {
}