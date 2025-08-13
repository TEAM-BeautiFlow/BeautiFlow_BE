package com.beautiflow.ManagedCustomer.service;

import static com.beautiflow.global.common.error.CustomerGroupErrorCode.CUSTOMER_GROUP_ERROR_CODE;
import static org.apache.logging.log4j.message.StructuredDataId.RESERVED;

import com.beautiflow.ManagedCustomer.domain.CustomerGroup;
import com.beautiflow.ManagedCustomer.domain.ManagedCustomer;
import com.beautiflow.ManagedCustomer.dto.CustomerDetailRes;
import com.beautiflow.ManagedCustomer.dto.CustomerGroupCreateReq;
import com.beautiflow.ManagedCustomer.dto.CustomerGroupRes;
import com.beautiflow.ManagedCustomer.dto.CustomerListSimpleRes;
import com.beautiflow.ManagedCustomer.dto.CustomerReservationItem;
import com.beautiflow.ManagedCustomer.dto.CustomerUpdateReq;
import com.beautiflow.ManagedCustomer.dto.CustomerUpdateRes;
import com.beautiflow.ManagedCustomer.repository.ManagedCustomerRepository;
import com.beautiflow.ManagedCustomer.repository.CustomerGroupRepository; // ★ 추가
import com.beautiflow.global.common.error.CustomerGroupErrorCode;
import com.beautiflow.global.common.error.ManagedCustomerErrorCode;
import com.beautiflow.global.common.error.CommonErrorCode;               // ★ 추가
import com.beautiflow.global.common.exception.BeautiFlowException;
import com.beautiflow.reservation.repository.ReservationRepository;
import com.beautiflow.shop.domain.Shop;
import com.beautiflow.user.domain.User;
import com.beautiflow.user.repository.UserRepository;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ManagedCustomerService {

  private final ManagedCustomerRepository managedCustomerRepository;
  private final ReservationRepository reservationRepository;
  private final CustomerGroupRepository customerGroupRepository; // ★ 추가

  @Transactional //디자이너와 고객이 이미 고객 관리에 등록돼 있는지 확인. -> 없으면 새로 등록
  public void autoRegister(User designer, User customer) {
    boolean exists = managedCustomerRepository.existsByDesignerAndCustomer(designer, customer);
    if (!exists) {
      ManagedCustomer entity = new ManagedCustomer(designer, customer, null);
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
  public CustomerUpdateRes updateCustomerInfo(Long designerId, Long customerId, CustomerUpdateReq req) {
    ManagedCustomer mc = managedCustomerRepository
        .findByDesignerIdAndCustomerId(designerId, customerId)
        .orElseThrow(() -> new BeautiFlowException(ManagedCustomerErrorCode.MANAGED_CUSTOMER_NOT_FOUND));

    mc.updateMemo(req.memo());

    List<CustomerGroup> groups = (req.groupIds() == null || req.groupIds().isEmpty())
        ? List.of()
        : customerGroupRepository.findAllById(req.groupIds());

    mc.replaceGroups(groups);

    return CustomerUpdateRes.of(customerId);
  }



  @Transactional(readOnly = true)
  public List<CustomerListSimpleRes> getCustomersByGroup(Long designerId, List<String> groups) {
    List<ManagedCustomer> customers;

    if (groups == null || groups.isEmpty()) {
      customers = managedCustomerRepository.findByDesignerId(designerId);
    } else {
      // 대소문자 섞여 들어와도 소문자로 정규화
      List<String> normalized = groups.stream()
          .filter(Objects::nonNull)
          .map(s -> s.trim())
          .filter(s -> !s.isEmpty())
          .map(s -> s.toLowerCase(java.util.Locale.ROOT))
          .distinct()
          .toList();

      if (normalized.isEmpty()) {
        customers = managedCustomerRepository.findByDesignerId(designerId);
      } else {
        customers = managedCustomerRepository.findByDesignerIdAndGroups_CodeIn(designerId, normalized);
      }
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
      managedCustomerRepository.save(new ManagedCustomer(designer, customer, null));
    }
  }

  private final CustomerGroupRepository repo;
  private final UserRepository userRepository;
  @Transactional
  public CustomerGroupRes create(Long designerId, CustomerGroupCreateReq req) {
    User designer = userRepository.findById(designerId)
        .orElseThrow(() -> new BeautiFlowException(CustomerGroupErrorCode.DESIGNER_NOT_FOUND));

    // 1) 정제 + 형식검증: 한글/영문(대소문자), 숫자, -, _
    String codeRaw = req.code() == null ? "" : req.code().trim();
    if (!codeRaw.matches("^[a-zA-Z0-9가-힣_-]{1,30}$")) {
      throw new BeautiFlowException(CustomerGroupErrorCode.INVALID_CODE);
    }

    // 저장은 소문자 정규화(권장), 비교는 항상 대소문자 무시
    String code = codeRaw.toLowerCase(java.util.Locale.ROOT);

    // 2) 예약(시스템) 코드 차단 — 대소문자 무시
    java.util.Set<String> RESERVED = java.util.Set.of("vip", "frequent", "blacklist");
    if (RESERVED.stream().anyMatch(r -> r.equalsIgnoreCase(code))
        || repo.existsByDesignerIsNullAndCodeIgnoreCase(code)) {
      throw new BeautiFlowException(CustomerGroupErrorCode.RESERVED_CODE);
    }

    // 3) 디자이너별 중복 코드 차단 — 대소문자 무시
    if (repo.existsByDesignerIdAndCodeIgnoreCase(designerId, code)) {
      throw new BeautiFlowException(CustomerGroupErrorCode.DUPLICATE_CODE);
    }

    // 4) 저장 (name 제거 버전)
    CustomerGroup saved = repo.save(CustomerGroup.custom(designer, code));
    return CustomerGroupRes.of(saved);
  }

}
