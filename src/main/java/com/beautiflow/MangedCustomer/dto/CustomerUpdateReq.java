package com.beautiflow.MangedCustomer.dto;

import com.beautiflow.global.domain.TargetGroup;

public record CustomerUpdateReq(
    String styleDescription,
    TargetGroup targetGroup
) {}
