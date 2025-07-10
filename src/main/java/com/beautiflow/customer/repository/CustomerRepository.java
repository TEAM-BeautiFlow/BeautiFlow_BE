package com.beautiflow.customer.repository;

import com.beautiflow.customer.domain.Customer;
import com.beautiflow.shop.domain.Shop;
import com.beautiflow.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

  // user_id + shop_id 기준으로 중복 고객 있는지 확인
  boolean existsByUserAndShop(User user, Shop shop);
}
