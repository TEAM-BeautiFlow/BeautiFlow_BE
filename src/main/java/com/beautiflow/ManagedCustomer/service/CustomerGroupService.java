package com.beautiflow.ManagedCustomer.service;

import com.beautiflow.ManagedCustomer.dto.CustomerGroupDetailRes;
import com.beautiflow.ManagedCustomer.repository.CustomerGroupRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerGroupService {

  private final CustomerGroupRepository customerGroupRepository;

  public List<CustomerGroupDetailRes> getAvailableGroups(Long designerId) {
    return customerGroupRepository.findAllForDesigner(designerId).stream()
        .map(CustomerGroupDetailRes::from)
        .toList();
  }
}
