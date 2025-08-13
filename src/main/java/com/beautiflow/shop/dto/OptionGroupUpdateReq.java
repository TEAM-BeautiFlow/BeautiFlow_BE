package com.beautiflow.shop.dto;

import java.util.List;

public record OptionGroupUpdateReq(
    Long id,              // 수정 시 그룹 ID, 신규 추가 시 null
    String name,
    List<OptionItemUpdateReq> items
) {}
