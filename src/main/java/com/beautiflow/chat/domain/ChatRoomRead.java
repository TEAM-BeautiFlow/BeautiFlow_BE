package com.beautiflow.chat.domain;

import java.time.LocalDateTime;

import com.beautiflow.user.domain.User;

import jakarta.persistence.Entity;
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

@Entity
@Table(name = "chat_room_reads")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomRead {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	private ChatRoom chatRoom;

	@ManyToOne(fetch = FetchType.LAZY)
	private User user;

	private LocalDateTime lastReadTime;

	private LocalDateTime lastAlertSentAt;
	private Long lastAlertFromMessageId;

	public void updateReadTime(LocalDateTime time) {
		this.lastReadTime = time;
	}

	public void markAlerted(Long messageId,LocalDateTime now){
		this.lastAlertFromMessageId=messageId;
		this.lastAlertSentAt=now;
	}
}