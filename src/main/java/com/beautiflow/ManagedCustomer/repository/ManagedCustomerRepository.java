package com.beautiflow.ManagedCustomer.repository;

import com.beautiflow.ManagedCustomer.domain.ManagedCustomer;
import com.beautiflow.global.domain.TargetGroup;
import com.beautiflow.user.domain.User;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import org.springframework.data.repository.query.Param;

public interface ManagedCustomerRepository extends JpaRepository<ManagedCustomer, Long> {


  boolean existsByDesignerAndCustomer(User designer, User customer);

  Optional<ManagedCustomer> findByDesignerIdAndCustomerId(Long designerId, Long customerId);

  List<ManagedCustomer> findByDesignerId(Long designerId);

  List<ManagedCustomer> findByDesignerIdAndTargetGroupIn(Long designerId, List<TargetGroup> groupEnums);

  @Query("""
      SELECT mc
      FROM ManagedCustomer mc
      JOIN FETCH mc.customer c
      WHERE mc.designer.id = :designerId
        AND (:keyword IS NULL OR c.name LIKE %:keyword%)
        AND (:groups IS NULL OR mc.targetGroup IN :groups)
      """)

  List<ManagedCustomer> findByDesignerIdAndTargetGroupInAndCustomerIdIn(
      Long designerId, List<TargetGroup> targetGroups, List<Long> customerIds);

  @Query("""
    SELECT mc
    FROM ManagedCustomer mc
    WHERE mc.designer.id = :designerId
      AND (:groups IS NULL OR mc.targetGroup IN :groups)
    ORDER BY mc.customer.name ASC
  """)
  List<ManagedCustomer> findByDesignerAndGroups(
      @Param("designerId") Long designerId,
      @Param("groups") Collection<TargetGroup> groups
  );
}
