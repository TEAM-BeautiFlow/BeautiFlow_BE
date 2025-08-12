package com.beautiflow.ManagedCustomer.service;

import com.beautiflow.ManagedCustomer.domain.ManagedCustomer;
import com.beautiflow.ManagedCustomer.dto.CustomerDetailRes;
import com.beautiflow.ManagedCustomer.dto.CustomerListSimpleRes;
import com.beautiflow.ManagedCustomer.dto.CustomerReservationItem;
import com.beautiflow.ManagedCustomer.dto.CustomerUpdateReq;
import com.beautiflow.ManagedCustomer.dto.CustomerUpdateRes;
import com.beautiflow.ManagedCustomer.repository.ManagedCustomerRepository;
import com.beautiflow.global.common.error.ManagedCustomerErrorCode;
import com.beautiflow.global.common.exception.BeautiFlowException;
import com.beautiflow.global.domain.TargetGroup;
import com.beautiflow.reservation.repository.ReservationRepository;
import com.beautiflow.shop.domain.Shop;
import com.beautiflow.user.domain.User;
import com.beautiflow.user.domain.UserStyle;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ManagedCustomerService {

  private final ManagedCustomerRepository managedCustomerRepository;
  private final ReservationRepository reservationRepository;

  @Transactional //디자이너와 고객이 이미 고객 관리에 등록돼 있는지 확인. -> 없으면 새로 등록
  public void autoRegister(User designer, User customer) {
    boolean exists = managedCustomerRepository.existsByDesignerAndCustomer(designer, customer);
    if (!exists) {
      ManagedCustomer entity = new ManagedCustomer(designer, customer, null, null);
      managedCustomerRepository.save(entity);
    }
  }

  @Transactional(readOnly = true)
  public CustomerDetailRes getCustomerDetail(Long designerId, Long customerId) {
    return managedCustomerRepository.findByDesignerIdAndCustomerId(designerId, customerId)
        .map(CustomerDetailRes::from)
        .orElse(null);
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

    // 그룹 업데이트 (그대로 유지)
    managed.updateInfo(req.targetGroup());

    // 메모 업데이트
    if (req.memo() != null) {
      managed.updateMemo(req.memo());
    }

    return CustomerUpdateRes.of(customerId);
  }


  @Transactional(readOnly = true)
  public List<CustomerListSimpleRes> getCustomersByGroup(Long designerId, List<String> groups) {
    List<ManagedCustomer> customers;

    if (groups == null || groups.isEmpty()) {
      customers = managedCustomerRepository.findByDesignerId(designerId);
    } else {
      List<TargetGroup> groupEnums = groups.stream()
          .map(String::toUpperCase)
          .map(TargetGroup::valueOf)
          .toList();

      customers = managedCustomerRepository.findByDesignerIdAndTargetGroupIn(designerId, groupEnums);
    }

    return customers.stream()
        .map(CustomerListSimpleRes::from)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<CustomerReservationItem> getCustomerReservationHistory(Long designerId, Long customerId) {
    return reservationRepository
        .findByDesignerIdAndCustomerIdWithAllRelations(designerId, customerId)
        .stream()
        .map(CustomerReservationItem::from)
        .toList();
  }

  @Transactional
  public void deleteCustomer(Long designerId, Long customerId) {
    ManagedCustomer managed = managedCustomerRepository
        .findByDesignerIdAndCustomerId(designerId, customerId)
        .orElseThrow(() -> new BeautiFlowException(ManagedCustomerErrorCode.MANAGED_CUSTOMER_NOT_FOUND));

    managedCustomerRepository.delete(managed);
  }

  @Transactional
  public void autoRegister(User designer, User customer, Shop shop) {
    boolean exists = managedCustomerRepository.existsByDesignerAndCustomer(designer, customer);
    if (!exists) {
      managedCustomerRepository.save(new ManagedCustomer(designer, customer, null, null));
    }
  }


}
