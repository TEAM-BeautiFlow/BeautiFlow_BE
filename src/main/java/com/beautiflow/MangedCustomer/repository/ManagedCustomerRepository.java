package com.beautiflow.MangedCustomer.repository;

import com.beautiflow.MangedCustomer.domain.ManagedCustomer;
import com.beautiflow.global.domain.TargetGroup;
import com.beautiflow.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ManagedCustomerRepository extends JpaRepository<ManagedCustomer, Long> {


  boolean existsByDesignerAndCustomer(User designer, User customer);

  @Query("""
      SELECT mc
      FROM ManagedCustomer mc
      JOIN FETCH mc.customer c
      WHERE mc.designer.id = :designerId
        AND (:keyword IS NULL OR c.name LIKE %:keyword%)
        AND (:groups IS NULL OR mc.targetGroup IN :groups)
      """)
  Page<ManagedCustomer> findFilteredByDesigner(
      @Param("designerId") Long designerId,
      @Param("keyword") String keyword,
      @Param("groups") List<TargetGroup> groups,
      Pageable pageable
  );

	List<ManagedCustomer> findByDesignerIdAndTargetGroupInAndCustomerIdIn(
		Long designerId, List<TargetGroup> targetGroups, List<Long> customerIds);
}
