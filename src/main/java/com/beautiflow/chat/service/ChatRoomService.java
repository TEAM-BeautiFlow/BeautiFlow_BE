package com.beautiflow.chat.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.beautiflow.chat.controller.ChatRoomRepository;
import com.beautiflow.chat.domain.ChatRoom;
import com.beautiflow.chat.dto.RoomCreateReq;
import com.beautiflow.chat.dto.RoomCreateRes;
import com.beautiflow.global.common.error.ChatRoomErrorCode;
import com.beautiflow.global.common.error.ShopErrorCode;
import com.beautiflow.global.common.error.UserErrorCode;
import com.beautiflow.global.common.exception.BeautiFlowException;
import com.beautiflow.shop.domain.Shop;
import com.beautiflow.shop.repository.ShopRepository;
import com.beautiflow.user.domain.User;
import com.beautiflow.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatRoomService {

	private final ChatRoomRepository chatRoomRepository;
	private final UserRepository userRepository;
	private final ShopRepository shopRepository;

	public RoomCreateRes createRoom(RoomCreateReq roomCreateReq){
		if (chatRoomRepository
			.findByShopIdAndCustomerIdAndDesignerId(roomCreateReq.shopId(), roomCreateReq.customerId(), roomCreateReq.designerId())
			.isPresent()) {
			throw new BeautiFlowException(ChatRoomErrorCode.CHAT_ROOM_ALREADY_EXISTS);
		}

		Shop shop = shopRepository.findById(roomCreateReq.shopId())
			.orElseThrow(() -> new BeautiFlowException(ShopErrorCode.SHOP_NOT_FOUND));
		User customer = userRepository.findById(roomCreateReq.customerId())
			.orElseThrow(() -> new BeautiFlowException(UserErrorCode.USER_NOT_FOUND));
		User designer = userRepository.findById(roomCreateReq.designerId())
			.orElseThrow(() -> new BeautiFlowException(UserErrorCode.USER_NOT_FOUND));

		ChatRoom newRoom = ChatRoom.builder()
			.shop(shop)
			.customer(customer)
			.designer(designer)
			.build();

		chatRoomRepository.save(newRoom);

		return RoomCreateRes.of(newRoom);

	}



}
