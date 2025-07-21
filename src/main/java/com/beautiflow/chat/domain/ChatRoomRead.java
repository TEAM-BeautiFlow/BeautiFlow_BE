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
import jakarta.persistence.Version;
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

	private LocalDateTime lastAlertSentTime;

	@Version
	private Long version;

	public void updateLastAlertSentTime(LocalDateTime now) {
		this.lastAlertSentTime = now;
	}

	public void updateReadTime(LocalDateTime time) {
		this.lastReadTime = time;
	}
}