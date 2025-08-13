package com.beautiflow.ManagedCustomer.dto;

import com.beautiflow.ManagedCustomer.domain.CustomerGroup;

public record CustomerGroupRes(
    Long id,
    String code,
    boolean isSystem
) {
  public static CustomerGroupRes of(CustomerGroup g) {
    return new CustomerGroupRes(g.getId(), g.getCode(), g.isSystem());
  }
}