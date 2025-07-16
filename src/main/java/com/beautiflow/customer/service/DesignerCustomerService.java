package com.beautiflow.customer.service;

import com.beautiflow.customer.domain.*;
import com.beautiflow.customer.dto.CustomerListRes;
import com.beautiflow.customer.repository.DesignerCustomerRepository;
import com.beautiflow.shop.domain.Shop;
import com.beautiflow.user.domain.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DesignerCustomerService {

  private final DesignerCustomerRepository designerCustomerRepository;

  @Transactional
  public void autoRegister(User designer, User customer, Shop shop) {
    DesignerCustomerId id = new DesignerCustomerId(designer.getId(), customer.getId(), shop.getId());

    if (!designerCustomerRepository.existsById(id)) {
      DesignerCustomer entity = DesignerCustomer.builder()
          .id(id)
          .designer(designer)
          .customer(customer)
          .shop(shop)
          .build();
      designerCustomerRepository.save(entity);
    }
  }

  @Transactional(readOnly = true)
  public List<CustomerListRes> getCustomersByDesigner(Long designerId) {
    return designerCustomerRepository.findByDesignerId(designerId).stream()
        .map(dc -> CustomerListRes.from(dc.getCustomer()))
        .toList();
  }



}
