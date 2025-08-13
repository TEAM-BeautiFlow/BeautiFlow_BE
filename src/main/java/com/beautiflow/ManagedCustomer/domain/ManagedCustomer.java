package com.beautiflow.ManagedCustomer.domain;

import static jakarta.persistence.FetchType.LAZY;

import com.beautiflow.global.domain.TargetGroup;
import com.beautiflow.user.domain.User;
import jakarta.persistence.*;
import java.util.List;
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

	@Enumerated(EnumType.STRING)
	private TargetGroup targetGroup;

	private String memo;

	public ManagedCustomer(User designer, User customer, TargetGroup targetGroup, String memo) {
		this.designer = designer;
		this.customer = customer;
		this.targetGroup = targetGroup;
		this.memo = memo;
	}

	public void updateInfo(TargetGroup targetGroup) {
		this.targetGroup = targetGroup;
	}

	public void updateMemo(String memo) {
		this.memo = memo;
	}

	public List<TargetGroup> getTargetGroups() {
		if (this.targetGroup != null) {
			return List.of(this.targetGroup);
		}
		return List.of();
	}

}


