package com.beautiflow.chat;

import java.util.ArrayList;
import java.util.List;

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
}