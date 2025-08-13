package com.beautiflow.ManagedCustomer.dto;

import java.util.List;

// CustomerUpdateReq
public record CustomerUpdateReq(
    List<Long> groupIds,
    Boolean clearGroup,   // ← 명시적으로 true면 그룹 해제
    String memo
) {}
