package com.beautiflow.chat.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.beautiflow.chat.repository.ChatRoomRepository;
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

	public RoomCreateRes createRoom(Long requesterId,RoomCreateReq roomCreateReq){
		Optional<ChatRoom> optional = chatRoomRepository
			.findByShopIdAndCustomerIdAndDesignerId(roomCreateReq.shopId(), roomCreateReq.customerId(), roomCreateReq.designerId());

		if (optional.isPresent()) {
			ChatRoom room = optional.get();

			// 재입장 처리
			User requester = userRepository.findById(requesterId)
				.orElseThrow(() -> new BeautiFlowException(UserErrorCode.USER_NOT_FOUND));
			room.reenterBy(requester);

			return RoomCreateRes.of(room);
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

	public void exitRoom(Long roomId, Long userId) {
		ChatRoom room = chatRoomRepository.findById(roomId)
			.orElseThrow(() -> new BeautiFlowException(ChatRoomErrorCode.CHATROOM_NOT_FOUND));

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new BeautiFlowException(UserErrorCode.USER_NOT_FOUND));

		if (!room.getCustomer().getId().equals(userId) && !room.getDesigner().getId().equals(userId)) {
			throw new BeautiFlowException(ChatRoomErrorCode.INVALID_CHATROOM_PARAMETER);
		}

		// 나가기 처리
		room.exitBy(user);

		//  둘 다 나갔으면 삭제
		if (room.isBothExited()) {
			chatRoomRepository.delete(room);
		}
	}




}
