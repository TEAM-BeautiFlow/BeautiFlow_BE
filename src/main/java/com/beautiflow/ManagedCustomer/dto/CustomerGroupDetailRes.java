package com.beautiflow.ManagedCustomer.dto;

import com.beautiflow.ManagedCustomer.domain.CustomerGroup;

public record CustomerGroupDetailRes(
    Long id,
    String code,
    boolean isSystem
) {
  public static CustomerGroupDetailRes from(CustomerGroup g) {
    return new CustomerGroupDetailRes(
        g.getId(),
        g.getCode(),
        g.isSystem()
    );
  }
}
