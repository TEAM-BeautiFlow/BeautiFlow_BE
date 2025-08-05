package com.beautiflow.ManagedCustomer.dto;

import com.beautiflow.global.domain.TargetGroup;

public record CustomerUpdateReq(
    String styleDescription,
    TargetGroup targetGroup
) {}