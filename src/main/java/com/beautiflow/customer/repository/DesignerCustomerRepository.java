package com.beautiflow.customer.repository;

import com.beautiflow.customer.domain.DesignerCustomer;
import com.beautiflow.customer.domain.DesignerCustomerId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DesignerCustomerRepository extends JpaRepository<DesignerCustomer, DesignerCustomerId> {
  List<DesignerCustomer> findByDesignerId(Long designerId);
}
