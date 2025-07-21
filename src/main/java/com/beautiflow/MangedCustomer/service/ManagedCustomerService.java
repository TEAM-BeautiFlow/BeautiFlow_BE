package com.beautiflow.MangedCustomer.service;

import com.beautiflow.MangedCustomer.domain.ManagedCustomer;
import com.beautiflow.MangedCustomer.dto.CustomerListRes;
import com.beautiflow.MangedCustomer.repository.ManagedCustomerRepository;
import com.beautiflow.global.common.error.ManagedCustomerErrorCode;
import com.beautiflow.global.common.exception.BeautiFlowException;
import com.beautiflow.shop.domain.Shop;
import com.beautiflow.user.domain.User;
import com.beautiflow.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ManagedCustomerService {

  private final ManagedCustomerRepository managedCustomerRepository;
  private final UserRepository userRepository;

  @Transactional
  public void autoRegister(User designer, User customer, Shop shop) {
    boolean exists = managedCustomerRepository.existsByDesignerAndCustomer(designer, customer);
    if (!exists) {
      ManagedCustomer entity = new ManagedCustomer(designer, customer, null, null);
      managedCustomerRepository.save(entity);
    }
  }

  @Transactional(readOnly = true)
  public List<CustomerListRes> getCustomersByDesigner(Long designerId) {
    // 디자이너 존재 여부 확인
    User designer = userRepository.findById(designerId)
        .orElseThrow(() -> new BeautiFlowException(ManagedCustomerErrorCode.LIST_NOT_FOUND));

    // 고객 리스트 조회
    List<CustomerListRes> customers = managedCustomerRepository.findByDesignerId(designerId).stream()
        .map(CustomerListRes::from)
        .toList();

    if (customers.isEmpty()) {
      throw new BeautiFlowException(ManagedCustomerErrorCode.LIST_NOT_FOUND);
    }

    return customers;
  }
}
