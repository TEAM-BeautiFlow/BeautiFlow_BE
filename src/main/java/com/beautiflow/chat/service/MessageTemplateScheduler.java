package com.beautiflow.chat.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.beautiflow.chat.domain.MessageTemplate;
import com.beautiflow.chat.domain.SendTiming;
import com.beautiflow.chat.dto.ChatMessageSendReq;
import com.beautiflow.chat.repository.MessageTemplateRepository;
import com.beautiflow.global.common.Alert.AlertEvent;
import com.beautiflow.global.common.Alert.AlertEventPublisher;
import com.beautiflow.global.domain.ReservationStatus;
import com.beautiflow.global.domain.SenderType;
import com.beautiflow.reservation.domain.Reservation;
import com.beautiflow.reservation.repository.ReservationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageTemplateScheduler {

	private final MessageTemplateRepository templateRepository;
	private final ReservationRepository reservationRepository;
	private final ChatRoomService chatRoomService;
	private final AlertEventPublisher alertEventPublisher;

	private final ObjectMapper objectMapper = new ObjectMapper()
		.registerModule(new JavaTimeModule())
		.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);


	@Scheduled(cron = "0 0 12 * * *")
	public void sendScheduledMessages() {
		LocalDate today = LocalDate.now();

		// 모든 활성화된 템플릿 조회
		List<MessageTemplate> activeTemplates = templateRepository.findByIsActiveTrue();

		for (MessageTemplate template : activeTemplates) {
			SendTiming timing = template.getSendTiming();
			int offset = template.getDaysOffset();

			List<Reservation> matchedReservations;

			if (timing == SendTiming.BEFORE_TREATMENT) {
				matchedReservations = reservationRepository.findByDesignerAndStatus(template.getOwner(), ReservationStatus.CONFIRMED)
					.stream()
					.filter(res -> res.getReservationDate().minusDays(offset).equals(today))
					.toList();

			} else {
				matchedReservations = reservationRepository.findByDesignerAndStatus(template.getOwner(), ReservationStatus.COMPLETED)
					.stream()
					.filter(res -> res.getReservationDate().plusDays(offset).equals(today))
					.toList();
			}

			if (matchedReservations.isEmpty()) {
				log.info("[스케줄러] '{}' 템플릿 전송 대상 없음 → skip", template.getName());
				continue;
			}

			for (Reservation reservation : matchedReservations) {
				Long roomId = chatRoomService
					.getOrCreateRoom(reservation.getShop(), reservation.getCustomer(), reservation.getDesigner())
					.getId();

				Long receiverId = reservation.getCustomer().getId();

				ChatMessageSendReq message = new ChatMessageSendReq(
					roomId,
					template.getOwner().getId(),
					SenderType.STAFF,
					template.getContent(),
					null
				);

				try {
					alertEventPublisher.publish(new AlertEvent(
						roomId,
						receiverId,
						template.getOwner().getId(),
						"TEMPLATE",
						template.getContent()
					));
				} catch (Exception e) {
					log.info("메시지 전송 실패: " + e.getMessage());
				}
			}
		}
	}

}