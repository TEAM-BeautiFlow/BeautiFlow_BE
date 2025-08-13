package com.beautiflow.ManagedCustomer.dto;

import com.beautiflow.ManagedCustomer.domain.ManagedCustomer;
import com.beautiflow.global.domain.TargetGroup;
import java.util.List;

public record CustomerListSimpleRes(
    Long customerId,
    String name,
    List<TargetGroup> targetGroups
) {
  public static CustomerListSimpleRes from(ManagedCustomer mc) {
    List<TargetGroup> groups = mc.getTargetGroups() != null
        ? mc.getTargetGroups()
        : (mc.getTargetGroup() != null ? List.of(mc.getTargetGroup()) : List.of());

    return new CustomerListSimpleRes(
        mc.getCustomer().getId(),
        mc.getCustomer().getName(),
        groups
    );
  }
}
