package com.beautiflow.ManagedCustomer.repository;

import com.beautiflow.ManagedCustomer.domain.CustomerGroup;
import com.beautiflow.ManagedCustomer.domain.ManagedCustomer;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerGroupRepository extends JpaRepository<CustomerGroup, Long> {
  boolean existsByDesignerIsNullAndCode(String code);
  boolean existsByDesignerIdAndCodeIgnoreCase(Long designerId, String code);
  boolean existsByDesignerIsNullAndCodeIgnoreCase(String code);

  @Query("""
    select mc from ManagedCustomer mc
    join mc.groups g
    where mc.designer.id = :designerId
      and lower(g.code) in :codes
""")
  List<ManagedCustomer> findByDesignerIdAndGroupsCodeInLower(
      @Param("designerId") Long designerId,
      @Param("codes") Collection<String> codesLower // 소문자로 넘기기
  );
}
