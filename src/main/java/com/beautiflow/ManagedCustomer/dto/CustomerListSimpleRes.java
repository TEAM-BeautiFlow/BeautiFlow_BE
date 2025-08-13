package com.beautiflow.ManagedCustomer.dto;

import com.beautiflow.ManagedCustomer.domain.ManagedCustomer;
import java.util.List;

public record CustomerListSimpleRes(
    Long customerId,
    String name,
    List<String> groupCodes
) {
  public static CustomerListSimpleRes from(ManagedCustomer mc) {
    List<String> codes = mc.getGroups().isEmpty()
        ? List.of()
        : mc.getGroups().stream()
            .map(g -> g.getCode())
            .toList();

    return new CustomerListSimpleRes(
        mc.getCustomer().getId(),
        mc.getCustomer().getName(),
        codes
    );
  }
}
