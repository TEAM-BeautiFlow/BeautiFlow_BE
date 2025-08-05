package com.beautiflow.ManagedCustomer.dto;

public record CustomerUpdateRes(Long customerId) {

  public static CustomerUpdateRes of(Long customerId) {
    return new CustomerUpdateRes(customerId);
  }
}