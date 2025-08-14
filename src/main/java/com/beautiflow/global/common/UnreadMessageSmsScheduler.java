package com.beautiflow.global.common;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.beautiflow.chat.domain.ChatMessage;
import com.beautiflow.chat.domain.ChatRoom;
import com.beautiflow.chat.domain.ChatRoomRead;
import com.beautiflow.chat.repository.ChatMessageRepository;
import com.beautiflow.chat.repository.ChatRoomReadRepository;
import com.beautiflow.chat.repository.ChatRoomRepository;
import com.beautiflow.global.common.sms.SmsService;
import com.beautiflow.user.domain.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class UnreadMessageSmsScheduler {
	private final ChatRoomRepository chatRoomRepository;
	private final ChatRoomReadRepository readRepository;
	private final ChatMessageRepository messageRepository;
	private final SmsService smsService;

	@Value("${alert.unread.threshold-minutes:60}")
	private int thresholdMinutes;

	@Value("${alert.unread.page-size:200}")
	private int pageSize;

	@Value("${alert.unread.enable-night-block:false}")
	private boolean enableNightBlock;

	@Value("${alert.unread.night-start:21:00}")
	private String nightStart;

	@Value("${alert.unread.night-end:08:00}")
	private String nightEnd;

	@Value("${app.chat.deep-link-prefix:https://beautiflow.co.kr/chat/rooms/}")
	private String deepLinkPrefix;

	private final AtomicBoolean running = new AtomicBoolean(false);

	private static final LocalDateTime FLOOR = LocalDateTime.of(1970, 1, 1, 0, 0);

	@Scheduled(cron = "0 */5 * * * *", zone = "Asia/Seoul")
	@Transactional
	public void run() {
		if (!running.compareAndSet(false, true)) {
			// 이전 작업이 아직 끝나지 않았으면 이번 턴은 스킵
			return;
		}
		try {
			processInBatches();
		} finally {
			running.set(false);
		}
	}

	@Transactional
	protected void processInBatches() {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime threshold = now.minusMinutes(thresholdMinutes);

		if (enableNightBlock && isInNightHours(now.toLocalTime())) {
			log.info("야간시간대이므로 미확인 SMS 전송 건너뜀");
			return;
		}

		int page = 0;
		Page<ChatRoom> rooms;
		do {
			rooms = chatRoomRepository.findAllActive(PageRequest.of(page++, pageSize));
			for (ChatRoom room : rooms) {
				if (room.getCustomer() != null && !room.isCustomerExited()) {
					checkAndNotify(room, room.getCustomer(), threshold, now);
				}
				if (room.getDesigner() != null && !room.isDesignerExited()) {
					checkAndNotify(room, room.getDesigner(), threshold, now);
				}
			}
		} while (rooms.hasNext());
	}

	private boolean isInNightHours(LocalTime now) {
		LocalTime start = LocalTime.parse(nightStart);
		LocalTime end   = LocalTime.parse(nightEnd);
		if (start.isBefore(end)) {
			return now.isAfter(start) && now.isBefore(end);
		} else { // 21:00 ~ 다음날 08:00 형태
			return now.isAfter(start) || now.isBefore(end);
		}
	}

	private void checkAndNotify(ChatRoom room, User recipient, LocalDateTime threshold, LocalDateTime now) {
		ChatRoomRead read = readRepository.findByChatRoomAndUser(room, recipient)
			.orElseGet(() -> readRepository.save(ChatRoomRead.builder()
				.chatRoom(room)
				.user(recipient)
				.lastReadTime(FLOOR)
				.build()));

		LocalDateTime lastRead = Optional.ofNullable(read.getLastReadTime()).orElse(FLOOR);
		if (lastRead.isBefore(FLOOR)) lastRead = FLOOR;

		Optional<ChatMessage> oldestUnread =
			messageRepository.findFirstByChatRoomAndCreatedTimeAfterAndSenderNotOrderByCreatedTimeAsc(
				room, lastRead, recipient
			);
		if (oldestUnread.isEmpty()) return;

		ChatMessage target = oldestUnread.get();

		if (read.getLastAlertFromMessageId() != null &&
			read.getLastAlertFromMessageId() >= target.getId()) {
			return;
		}

		// 1시간(설정값) 이상 경과
		if (target.getCreatedTime().isAfter(threshold)) return;

		String phone = recipient.getContact(); // 실제 필드명에 맞게
		if (phone == null || phone.isBlank()) return;

		String link = deepLinkPrefix + room.getId();
		String shopName = room.getShop() != null ? room.getShop().getShopName() : "매장";
		smsService.sendUnreadReminder(phone, shopName, link);

		read.markAlerted(target.getId(), now);
		readRepository.save(read);
	}

}


