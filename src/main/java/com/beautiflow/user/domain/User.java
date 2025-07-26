package com.beautiflow.user.domain;

import java.util.ArrayList;
import java.util.List;

import com.beautiflow.global.domain.BaseTimeEntity;
import com.beautiflow.reservation.domain.Reservation;
import com.beautiflow.shop.domain.ShopMember;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
@SQLDelete(sql = "UPDATE users SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
public class User extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String kakaoId;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private String contact;

	private String intro;

	private boolean deleted = false;

	@OneToMany(mappedBy = "user")
	private List<UserRole> roles = new ArrayList<>();

	@OneToMany(mappedBy = "user")
	private List<ShopMember> shopMemberships = new ArrayList<>();

	@OneToMany(mappedBy = "customer")
	private List<Reservation> reservations = new ArrayList<>();

	@OneToMany(mappedBy = "user")
	private List<UserStyleImage> styleImages = new ArrayList<>();


	public void reactivate(String name, String contact) {
		this.deleted = false;
		this.name = name;
		this.contact = contact;
	}


}
