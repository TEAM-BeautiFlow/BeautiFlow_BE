package com.beautiflow.ManagedCustomer.boot;

import com.beautiflow.ManagedCustomer.domain.CustomerGroup;
import com.beautiflow.ManagedCustomer.repository.CustomerGroupRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomerGroupInitializer implements ApplicationRunner {
  private final CustomerGroupRepository repo;

  @Override @Transactional
  public void run(ApplicationArguments args) {
    ensure("VIP","VIP");
    ensure("자주 오는 고객","FREQUENT");
    ensure("블랙리스트","BLACKLIST");
  }

  private void ensure(String name, String code) {
    if (repo.existsByDesignerIsNullAndCode(code)) return;
    repo.save(CustomerGroup.builder()
        .designer(null)
        .code(code)
        .isSystem(true)
        .build());
  }

}
