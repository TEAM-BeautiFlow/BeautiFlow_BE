package com.beautiflow.customer.dto;

import com.beautiflow.user.domain.User;

public record CustomerListRes(
    Long customerId,
    String name,
    String contact // 변경된 필드명 반영
) {
  public static CustomerListRes from(User customer) {
    return new CustomerListRes(
        customer.getId(),
        customer.getName(),
        customer.getContact()
    );
  }
}
