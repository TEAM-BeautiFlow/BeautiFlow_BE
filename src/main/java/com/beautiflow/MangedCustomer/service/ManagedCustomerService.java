package com.beautiflow.MangedCustomer.service;

import com.beautiflow.MangedCustomer.domain.ManagedCustomer;
import com.beautiflow.MangedCustomer.dto.CustomerListRes;
import com.beautiflow.MangedCustomer.dto.CustomerUpdateReq;
import com.beautiflow.MangedCustomer.dto.CustomerUpdateRes;
import com.beautiflow.MangedCustomer.repository.ManagedCustomerRepository;
import com.beautiflow.global.common.error.ManagedCustomerErrorCode;
import com.beautiflow.global.common.exception.BeautiFlowException;
import com.beautiflow.global.domain.TargetGroup;
import com.beautiflow.user.domain.User;
import com.beautiflow.user.domain.UserStyle;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ManagedCustomerService {

  private final ManagedCustomerRepository managedCustomerRepository;

  @Transactional // 디자이너와 고객이 이미 고객 관리에 등록돼 있는지 확인. -> 없으면 새로 등록
  public void autoRegister(User designer, User customer) {
    boolean exists = managedCustomerRepository.existsByDesignerAndCustomer(designer, customer);
    if (!exists) {
      ManagedCustomer entity = new ManagedCustomer(designer.getId(), customer, null, null);
      managedCustomerRepository.save(entity);
    }
  }

  @Transactional
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

  @Transactional
  public CustomerUpdateRes updateCustomerInfo(
      Long designerId,
      Long customerId,
      CustomerUpdateReq req
  ) {
    ManagedCustomer managed = managedCustomerRepository
        .findByDesignerIdAndCustomerId(designerId, customerId)
        .orElseThrow(() -> new BeautiFlowException(ManagedCustomerErrorCode.MANAGED_CUSTOMER_ERROR_CODE));

    managed.updateInfo(req.styleDescription(), req.targetGroup());

    User customer = managed.getCustomer();
    UserStyle style = customer.getStyle(); // 연관관계 필요

    if (style != null) {
      style.setDescription(req.styleDescription());
    }

    return CustomerUpdateRes.of(customerId);
  }


}
