package com.beautiflow.MangedCustomer.service;

import com.beautiflow.MangedCustomer.domain.ManagedCustomer;
import com.beautiflow.MangedCustomer.dto.CustomerListRes;
import com.beautiflow.MangedCustomer.repository.ManagedCustomerRepository;
import com.beautiflow.user.domain.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ManagedCustomerService {

  private final ManagedCustomerRepository managedCustomerRepository;

  @Transactional //디자이너와 고객이 이미 고객 관리에 등록돼 있는지 확인. -> 없으면 새로 등록
  public void autoRegister(User designer, User customer) {
    boolean exists = managedCustomerRepository.existsByDesignerAndCustomer(designer, customer);
    if (!exists) {
      ManagedCustomer entity = new ManagedCustomer(designer, customer, null, null);
      managedCustomerRepository.save(entity);
    }
  }

  @Transactional(readOnly = true)
  public List<CustomerListRes> getCustomersByDesigner(Long designerId) {
    return managedCustomerRepository.findByDesignerId(designerId).stream()
        .map(CustomerListRes::from)
        .toList(); // 고객 없으면 빈 리스트 반환됨
  }
}
