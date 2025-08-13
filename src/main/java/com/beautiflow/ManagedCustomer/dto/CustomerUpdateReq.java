package com.beautiflow.ManagedCustomer.dto;

import jakarta.validation.constraints.Size;
import java.util.List;

// CustomerUpdateReq
public record CustomerUpdateReq(
    List<Long> groupIds,
    Boolean clearGroup,   // ← 명시적으로 true면 그룹 해제
    String memo
) {}
