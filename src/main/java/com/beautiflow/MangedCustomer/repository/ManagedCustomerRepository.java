package com.beautiflow.MangedCustomer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.beautiflow.MangedCustomer.domain.ManagedCustomer;
import com.beautiflow.chat.domain.ChatMessage;
import com.beautiflow.global.domain.TargetGroup;

public interface ManagedCustomerRepository extends JpaRepository<ManagedCustomer,Long> {
	List<ManagedCustomer> findByDesignerIdAndTargetGroupInAndCustomerIdIn(
		Long designerId, List<TargetGroup> targetGroups, List<Long> customerIds);
}
