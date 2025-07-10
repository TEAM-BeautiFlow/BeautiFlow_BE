package com.beautiflow.customer.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.beautiflow.customer.domain.Customer;
import com.beautiflow.customer.repository.CustomerRepository;
import com.beautiflow.reservation.domain.Reservation;
import com.beautiflow.shop.domain.Shop;
import com.beautiflow.user.domain.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomerService {

  private final CustomerRepository customerRepository;

  @Transactional
  public void autoRegister(Reservation reservation) {
    User user = reservation.getCustomer();
    Shop shop = reservation.getShop();

    // 예외 처리 또는 로그를 위해 null 체크 가능
    if (user == null || shop == null) return;

    // 이미 고객으로 등록돼 있다면 패스
    if (customerRepository.existsByUserAndShop(user, shop)) return;

    Customer newCustomer = Customer.builder()
        .user(user)
        .shop(shop)
        .name(user.getName())
        .phone(user.getContact())
        .build();

    customerRepository.save(newCustomer);
  }
}
