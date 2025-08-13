package com.beautiflow.ManagedCustomer.dto;

import java.util.List;

// CustomerUpdateReq
public record CustomerUpdateReq(
    List<Long> groupIds,
    String memo
) {}
