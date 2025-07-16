package com.beautiflow.user.domain;

import java.io.Serializable;

import com.beautiflow.global.domain.GlobalRole;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Embeddable
public class UserRoleId implements Serializable {
	private Long userId;

	@Enumerated(EnumType.STRING)
	private GlobalRole role;
}
