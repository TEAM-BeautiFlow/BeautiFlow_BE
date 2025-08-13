package com.beautiflow.chat.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.beautiflow.ManagedCustomer.domain.ManagedCustomer;
import com.beautiflow.ManagedCustomer.repository.ManagedCustomerRepository;
import com.beautiflow.chat.domain.ChatMessage;
import com.beautiflow.chat.domain.ChatRoom;
import com.beautiflow.chat.dto.chatMessageDto.ChatMessageSendReq;
import com.beautiflow.chat.dto.chatMessageDto.GroupMessageSendReq;
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

		Set<User> targets = new LinkedHashSet<>();

		if (req.groupCodes() != null && !req.groupCodes().isEmpty()) {
			List<ManagedCustomer> byGroups = managedCustomerRepository.findByDesignerIdAndGroupCodes(designerId, req.groupCodes());
			byGroups.stream().map(ManagedCustomer::getCustomer).forEach(targets::add);
		}
		if (req.customerIds() != null && !req.customerIds().isEmpty()) {
			List<ManagedCustomer> byCustomers = managedCustomerRepository.findByDesignerIdAndCustomerIds(designerId, req.customerIds());
			byCustomers.stream().map(ManagedCustomer::getCustomer).forEach(targets::add);
		}

		if (targets.isEmpty()) {
			throw new BeautiFlowException(UserErrorCode.USER_NOT_FOUND);
		}

		List<Long> customerIdList = targets.stream().map(User::getId).toList();
		Map<Long, ChatRoom> existingRoomsByCustomerId = chatRoomRepository
			.findByShopIdAndDesignerIdAndCustomerIdIn(req.shopId(), designer.getId(), customerIdList)
			.stream()
			.collect(Collectors.toMap(cr -> cr.getCustomer().getId(), Function.identity()));

		List<ChatRoom> roomsToCreate = new ArrayList<>();

		for (User customer : targets) {
			ChatRoom room = existingRoomsByCustomerId.get(customer.getId());
			if (room == null) {
				room = ChatRoom.builder()
					.shop(shop)
					.customer(customer)
					.designer(designer)
					.build();
				roomsToCreate.add(room);
				existingRoomsByCustomerId.put(customer.getId(), room);
			} else {
				room.reEnterBy(designer);
				if (room.isCustomerExited()) {
					room.reEnterBy(customer);
				}
			}
		}

		if (!roomsToCreate.isEmpty()) {
			chatRoomRepository.saveAll(roomsToCreate);
		}
		for (User customer : targets) {
			ChatRoom room = existingRoomsByCustomerId.get(customer.getId());

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