package com.beautiflow.chat.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.beautiflow.chat.domain.ChatMessage;
import com.beautiflow.global.domain.BaseTimeEntity;
import com.beautiflow.shop.domain.Shop;
import com.beautiflow.user.domain.User;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "chat_rooms")
public class ChatRoom extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	private Shop shop;

	@ManyToOne(fetch = FetchType.LAZY)
	private User customer;

	@ManyToOne(fetch = FetchType.LAZY)
	private User designer;

	@OneToMany(mappedBy = "chatRoom")
	private List<ChatMessage> messages = new ArrayList<>();

	private boolean customerExited = false;
	private boolean designerExited = false;

	public boolean isBothExited() {
		return customerExited && designerExited;
	}

	public void exitBy(User user) {
		Long uid = user.getId();
		if (Objects.equals(this.customer.getId(), uid)) {
			this.customerExited = true;
		} else if (Objects.equals(this.designer.getId(), uid)) {
			this.designerExited = true;
		} else {
			throw new IllegalArgumentException("채팅방에 속하지 않은 유저입니다.");
		}
	}
	public void reEnterBy(User sender) {
		if (Objects.equals(this.customer.getId(), sender.getId())) {
			this.customerExited = false;
		} else if (Objects.equals(this.designer.getId(), sender.getId())) {
			this.designerExited = false;
		} else {
			throw new IllegalArgumentException("채팅방에 속하지 않은 유저입니다.");
		}
	}

}
