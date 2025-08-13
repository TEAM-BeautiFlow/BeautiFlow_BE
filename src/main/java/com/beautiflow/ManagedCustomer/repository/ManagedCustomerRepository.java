package com.beautiflow.ManagedCustomer.repository;

import com.beautiflow.ManagedCustomer.domain.ManagedCustomer;
import com.beautiflow.global.domain.TargetGroup;
import com.beautiflow.user.domain.User;

import java.util.Collection;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ManagedCustomerRepository extends JpaRepository<ManagedCustomer, Long> {

  boolean existsByDesignerAndCustomer(User designer, User customer);

  Optional<ManagedCustomer> findByDesignerIdAndCustomerId(Long designerId, Long customerId);

  List<ManagedCustomer> findByDesignerId(Long designerId);

  List<ManagedCustomer> findByDesignerIdAndGroups_CodeIn(Long designerId, List<String> codes);

  List<ManagedCustomer> findByDesignerIdAndTargetGroupInAndCustomerIdIn(
      Long designerId, List<TargetGroup> targetGroups, List<Long> customerIds);

  @Query("""
      SELECT mc
      FROM ManagedCustomer mc
      JOIN FETCH mc.customer c
      LEFT JOIN mc.groups g
      WHERE mc.designer.id = :designerId
        AND (:keyword IS NULL OR c.name LIKE %:keyword%)
        AND (:groupIds IS NULL OR (g IS NOT NULL AND g.id IN :groupIds))
      """)
  List<ManagedCustomer> searchByDesignerWithOptionalGroups(
      Long designerId, String keyword, List<Long> groupIds);

  @Query("""
        select distinct mc
        from ManagedCustomer mc
        join mc.groups g
        where mc.designer.id = :designerId
          and g.code in :groupCodes
    """)
  List<ManagedCustomer> findByDesignerIdAndGroupCodes(
      @Param("designerId") Long designerId,
      @Param("groupCodes") Collection<String> groupCodes
  );

  @Query("""
        select mc
        from ManagedCustomer mc
        where mc.designer.id = :designerId
          and mc.customer.id in :customerIds
    """)
  List<ManagedCustomer> findByDesignerIdAndCustomerIds(
      @Param("designerId") Long designerId,
      @Param("customerIds") Collection<Long> customerIds
  );

}
