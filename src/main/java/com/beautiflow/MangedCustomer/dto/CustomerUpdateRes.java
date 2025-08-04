package com.beautiflow.MangedCustomer.dto;

public record CustomerUpdateRes(Long customerId) {

  public static CustomerUpdateRes of(Long customerId) {
    return new CustomerUpdateRes(customerId);
  }
}
