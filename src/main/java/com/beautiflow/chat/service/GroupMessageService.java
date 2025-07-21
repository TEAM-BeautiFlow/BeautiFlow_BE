package com.beautiflow.chat.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.beautiflow.MangedCustomer.domain.ManagedCustomer;
import com.beautiflow.MangedCustomer.repository.ManagedCustomerRepository;
import com.beautiflow.chat.domain.ChatMessage;
import com.beautiflow.chat.domain.ChatRoom;
import com.beautiflow.chat.dto.ChatMessageSendReq;
import com.beautiflow.chat.dto.GroupMessageSendReq;
import com.beautiflow.chat.repository.ChatMessageRepository;
import com.beautiflow.chat.repository.ChatRoomRepository;
import com.beautiflow.global.common.error.ShopErrorCode;
import com.beautiflow.global.common.error.UserErrorCode;
import com.beautiflow.global.common.exception.BeautiFlowException;
import com.beautiflow.global.domain.SenderType;
import com.beautiflow.shop.domain.Shop;
import com.beautiflow.shop.repository.ShopRepository;
import com.beautiflow.user.domain.User;
import com.beautiflow.user.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class GroupMessageService {

	private final ManagedCustomerRepository managedCustomerRepository;
	private final ChatRoomRepository chatRoomRepository;
	private final ChatMessageRepository chatMessageRepository;
	private final UserRepository userRepository;
	private final ShopRepository shopRepository;
	private final RedisPubSubService redisPubSubService;

	public void sendGroupMessage(Long designerId, GroupMessageSendReq req) {
		User designer = userRepository.findById(designerId)
			.orElseThrow(() -> new BeautiFlowException(UserErrorCode.USER_NOT_FOUND));

		Shop shop = shopRepository.findById(req.shopId())
			.orElseThrow(() -> new BeautiFlowException(ShopErrorCode.SHOP_NOT_FOUND));

		// 여러 TargetGroup
		List<User> targetCustomers = managedCustomerRepository
			.findByDesignerIdAndTargetGroupInAndCustomerIdIn(designerId, req.targetGroups(), req.customerIds())
			.stream()
			.map(ManagedCustomer::getCustomer)
			.distinct() // 중복 제거
			.toList();

		for (User customer : targetCustomers) {
			ChatRoom room = chatRoomRepository.findByShopIdAndCustomerIdAndDesignerId(
				req.shopId(), customer.getId(), designerId
			).map(existing -> {
				existing.reEnterBy(designer);

				if (existing.isCustomerExited()) {
					existing.reEnterBy(customer);
				}

				return existing;
			}).orElseGet(() -> {
				ChatRoom newRoom = ChatRoom.builder()
					.shop(shop)
					.customer(customer)
					.designer(designer)
					.build();
				return chatRoomRepository.save(newRoom);
			});

			ChatMessage message = ChatMessage.builder()
				.chatRoom(room)
				.sender(designer)
				.senderType(SenderType.STAFF)
				.content(req.content())
				.imageUrl(req.imageUrl())
				.build();

			chatMessageRepository.save(message);

			ChatMessageSendReq messageDto = new ChatMessageSendReq(
				room.getId(),
				designer.getId(),
				SenderType.STAFF,
				req.content(),
				req.imageUrl()
			);

			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.registerModule(new JavaTimeModule());
			objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

			String payload;
			try {
				payload = objectMapper.writeValueAsString(messageDto);
				redisPubSubService.publish("chatroom", payload);
			} catch (JsonProcessingException e) {
				log.error("메시지 직렬화 실패: {}", e.getMessage());
			}

		}
	}
}