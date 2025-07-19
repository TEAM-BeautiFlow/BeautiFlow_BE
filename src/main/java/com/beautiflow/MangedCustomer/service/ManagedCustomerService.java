package com.beautiflow.MangedCustomer.service;

import com.beautiflow.MangedCustomer.domain.ManagedCustomer;
import com.beautiflow.MangedCustomer.dto.CustomerListRes;
import com.beautiflow.MangedCustomer.repository.ManagedCustomerRepository;
import com.beautiflow.shop.domain.Shop;
import com.beautiflow.user.domain.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ManagedCustomerService {

  private final ManagedCustomerRepository managedCustomerRepository;

  @Transactional
  public void autoRegister(User designer, User customer, Shop shop) {
    boolean exists = managedCustomerRepository.existsByDesignerAndCustomer(designer, customer);
    if (!exists) {
      ManagedCustomer entity = new ManagedCustomer(designer, customer, null, null); // memo, targetGroup은 현재 null로
      managedCustomerRepository.save(entity);
    }
  }

  @Transactional(readOnly = true)
  public List<CustomerListRes> getCustomersByDesigner(Long designerId) {
    return managedCustomerRepository.findByDesignerId(designerId).stream()
        .map(CustomerListRes::from)
        .toList();
  }
}
