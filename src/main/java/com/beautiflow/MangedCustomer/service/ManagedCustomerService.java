package com.beautiflow.MangedCustomer.service;

import com.beautiflow.MangedCustomer.domain.ManagedCustomer;
import com.beautiflow.MangedCustomer.dto.CustomerListRes;
import com.beautiflow.MangedCustomer.repository.ManagedCustomerRepository;
import com.beautiflow.global.domain.TargetGroup;
import com.beautiflow.user.domain.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
  public Page<CustomerListRes> getCustomersByDesigner(
      Long designerId,
      String keyword,
      List<TargetGroup> groups,
      Pageable pageable
  ) {
    return managedCustomerRepository
        .findFilteredByDesigner(designerId, keyword, groups, pageable)
        .map(CustomerListRes::from);
  }

}
