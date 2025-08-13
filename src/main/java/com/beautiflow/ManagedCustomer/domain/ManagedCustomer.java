package com.beautiflow.ManagedCustomer.domain;

import static jakarta.persistence.FetchType.LAZY;

import com.beautiflow.user.domain.User;
import jakarta.persistence.*;
import java.util.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(
		name = "managed_customer",
		uniqueConstraints = @UniqueConstraint(
				name = "uk_designer_customer",
				columnNames = {"designer_id", "customer_id"}
		),
		indexes = {
				@Index(name = "idx_designer", columnList = "designer_id"),
				@Index(name = "idx_customer", columnList = "customer_id")
		}
)
public class ManagedCustomer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = LAZY)
	private User designer;

	@ManyToOne(fetch = LAZY)
	private User customer;

	@ManyToMany
	@JoinTable(
			name = "managed_customer_groups",
			joinColumns = @JoinColumn(name = "managed_customer_id"),
			inverseJoinColumns = @JoinColumn(name = "group_id")
	)
	@Builder.Default
	private Set<CustomerGroup> groups = new HashSet<>();

	private String memo;

	public void replaceGroups(Collection<CustomerGroup> target) {
		this.groups.clear();
		if (target != null) this.groups.addAll(target);
	}

	public void updateMemo(String memo) {
		this.memo = memo;
	}

	public ManagedCustomer(User designer, User customer, String memo) {
		this.designer = designer;
		this.customer = customer;
		this.groups = new HashSet<>();
		this.memo = memo;
	}

}
