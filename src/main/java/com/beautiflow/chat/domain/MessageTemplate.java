package com.beautiflow.chat.domain;

import com.beautiflow.chat.dto.MessageTemplateUpdateReq;
import com.beautiflow.global.domain.BaseTimeEntity;
import com.beautiflow.user.domain.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "message_templates")
public class MessageTemplate extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name; // 템플릿명

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private SendTiming sendTiming;

	private int daysOffset;

	private String content;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TargetGroup targetGroup;

	private boolean isActive;

	@ManyToOne(fetch = FetchType.LAZY)
	private User owner; //디자이너랑 사장만


	public void update(MessageTemplateUpdateReq req) {
		this.name = req.name();
		this.sendTiming = req.sendTiming();
		this.daysOffset = req.daysOffset();
		this.content = req.content();
		this.targetGroup = req.targetGroup();
		this.isActive = req.isActive();
	}

}