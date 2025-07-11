/*
package com.beautiflow.chat.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.beautiflow.chat.domain.ChatRoom;
import com.beautiflow.chat.domain.MessageTemplate;
import com.beautiflow.chat.domain.SendTiming;
import com.beautiflow.chat.dto.ChatMessageSendReq;
import com.beautiflow.chat.repository.ChatRoomRepository;
import com.beautiflow.chat.repository.MessageTemplateRepository;
import com.beautiflow.global.domain.SenderType;
import com.beautiflow.reservation.domain.Reservation;
import com.beautiflow.reservation.repository.ReservationRepository;
import com.beautiflow.user.domain.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MessageTemplateScheduler {

	private final MessageTemplateRepository templateRepository;
	private final ReservationRepository reservationRepository;
	private final ChatRoomService chatRoomService;
	private final ChatMessageService chatMessageService;
	private final RedisPubSubService pubSubService;
	private final MessageTemplateService templateService;

	private final ObjectMapper objectMapper = new ObjectMapper()
		.registerModule(new JavaTimeModule())
		.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

	*/
/**
	 * 매일 09:00에 실행
	 *//*

	@Scheduled(cron = "0 0 9 * * *")
	@Transactional
	public void autoSendTemplates() {
		List<MessageTemplate> activeTemplates = templateRepository.findAll().stream()
			.filter(MessageTemplate::isActive)
			.toList();

		for (MessageTemplate template : activeTemplates) {
			LocalDate targetDate = calcTargetDate(template.getSendTiming(), template.getDaysOffset());

			// 조건에 맞는 예약 조회
			List<Reservation> reservations = reservationRepository
				.findByDesignerAndTargetGroupAndReservationDate(
					template.getOwner(),
					template.getTargetGroup(),
					targetDate
				);

			for (Reservation reservation : reservations) {
				User customer = reservation.getCustomer();
				User designer = reservation.getDesigner();

				Optional<ChatRoom> optionalRoom = chatRoomService.findByShopAndCustomerAndDesigner(
					reservation.getShop(), customer, designer
				);

				ChatRoom room;

				// 2. 없으면 생성
				if (optionalRoom.isPresent()) {
					room = optionalRoom.get();
				} else {
					room = chatRoomService.createRoom(reservation.getShop(), customer, designer);
				}

				// 템플릿을 메시지로 변환
				ChatMessageSendReq dto = ChatMessageSendReq.fromTemplate(room.getId(), template, designer);

				// 메시지 저장
				chatMessageService.saveMessage(room.getId(), dto);

				// 실시간 전송
				try {
					String serialized = objectMapper.writeValueAsString(dto);
					pubSubService.publish("chat", serialized);
				} catch (Exception e) {
					throw new RuntimeException("템플릿 메시지 직렬화 실패", e);
				}
			}
		}
	}

	private LocalDate calcTargetDate(SendTiming timing, int offset) {
		return LocalDate.now().plusDays(timing == SendTiming.BEFORE_TREATMENT ? offset : -offset);
	}
}

*/
