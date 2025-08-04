package com.beautiflow.MangedCustomer.dto;

import com.beautiflow.MangedCustomer.domain.ManagedCustomer;
import com.beautiflow.global.domain.TargetGroup;

public record CustomerListRes(
    Long customerId,
    String name,
    String contact,
    TargetGroup targetGroup,
    String styleDescription // ← 이름 변경 권장
) {
  public static CustomerListRes from(ManagedCustomer managedCustomer) {
    return new CustomerListRes(
        managedCustomer.getCustomer().getId(),
        managedCustomer.getCustomer().getName(),
        managedCustomer.getCustomer().getContact(),
        managedCustomer.getTargetGroup(),
        managedCustomer.getCustomer().getStyle() != null
            ? managedCustomer.getCustomer().getStyle().getDescription()
            : null
    );
  }
}
