package com.beautiflow.MangedCustomer.domain;

import static jakarta.persistence.FetchType.*;

import com.beautiflow.global.domain.TargetGroup;
import com.beautiflow.user.domain.User;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class ManagedCustomer {

	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne(fetch = LAZY)
	private User designer;

	@ManyToOne(fetch = LAZY)
	private User customer;

	@Enumerated(EnumType.STRING)
	private TargetGroup targetGroup;

	private String memo;
}