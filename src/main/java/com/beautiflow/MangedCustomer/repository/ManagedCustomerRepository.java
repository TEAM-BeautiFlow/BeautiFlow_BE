package com.beautiflow.MangedCustomer.repository;

import com.beautiflow.MangedCustomer.domain.ManagedCustomer;
import com.beautiflow.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ManagedCustomerRepository extends JpaRepository<ManagedCustomer, Long> {
  List<ManagedCustomer> findByDesignerId(Long designerId);
  boolean existsByDesignerAndCustomer(User designer, User customer);

}
