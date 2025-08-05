package com.beautiflow.ManagedCustomer.dto;

import com.beautiflow.ManagedCustomer.domain.ManagedCustomer;
import com.beautiflow.global.domain.TargetGroup;

public record CustomerListSimpleRes(
    Long customerId,
    String name,
    TargetGroup targetGroup
) {
  public static CustomerListSimpleRes from(ManagedCustomer mc) {
    return new CustomerListSimpleRes(
        mc.getCustomer().getId(),
        mc.getCustomer().getName(),
        mc.getTargetGroup()
    );
  }
}
