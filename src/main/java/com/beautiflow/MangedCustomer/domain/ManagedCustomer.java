package com.beautiflow.MangedCustomer.domain;

import static jakarta.persistence.FetchType.LAZY;

import com.beautiflow.global.domain.TargetGroup;
import com.beautiflow.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
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

	public void updateInfo(TargetGroup targetGroup) {
		this.targetGroup = targetGroup;
	}

}


