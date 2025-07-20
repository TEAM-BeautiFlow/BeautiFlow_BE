package com.beautiflow.chat.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.beautiflow.chat.domain.ChatMessage;
import com.beautiflow.chat.repository.ChatMessageRepository;
import com.beautiflow.chat.repository.ChatRoomReadRepository;
import com.beautiflow.chat.repository.ChatRoomRepository;
import com.beautiflow.global.common.Alert.AlertEvent;
import com.beautiflow.global.common.Alert.AlertEventPublisher;
import com.beautiflow.global.common.sms.SmsService;
import com.beautiflow.user.domain.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class ChatMessageAlertScheduler {

	private final ChatMessageRepository chatMessageRepository;
	private final ChatRoomReadRepository chatRoomReadRepository;
	private final AlertEventPublisher alertEventPublisher;
	private final ChatRoomRepository chatRoomRepository;

	// 1시간마다, 매시 10분에 실행 (ex. 01:10, 02:10 ...)
	@Scheduled(cron = "0 10 * * * *")
	@Transactional
	public void sendUnreadMessageAlerts() {
		LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);

		List<ChatMessage> messages = chatMessageRepository.findLatestUnreadMessagesForAlert(oneHourAgo);

		for (ChatMessage message : messages) {
			User receiver = getReceiver(message);

			chatRoomReadRepository.findByChatRoomAndUser(message.getChatRoom(), receiver).ifPresent(read -> {
				try {
					Long roomId = message.getChatRoom().getId();
					// SMS 전송
					alertEventPublisher.publish(new AlertEvent(
						roomId,
						receiver.getId(),
						null,
						"SMS",
						null
					));

					// 마지막 알림 시간 갱신
					read.updateLastAlertSentTime(LocalDateTime.now());

					// 저장 시 낙관적 락 적용됨
					chatRoomReadRepository.save(read);
					log.info("📨 [{}]님에게 SMS 알림 전송 이벤트 큐 등록 완료 (ChatRoom: {})", receiver.getName(), roomId);

				} catch (ObjectOptimisticLockingFailureException e) {
					log.warn("⚠️ 알림 전송 중 충돌 발생: ChatRoomRead ID={}, User={}", read.getId(), receiver.getId());
				}
			});
		}
	}

	private User getReceiver(ChatMessage message) {
		var room = message.getChatRoom();
		var sender = message.getSender();

		return sender.getId().equals(room.getCustomer().getId())
			? room.getDesigner()
			: room.getCustomer();
	}
}
