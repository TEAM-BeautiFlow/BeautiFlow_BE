package com.beautiflow.chat.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.beautiflow.chat.domain.ChatMessage;
import com.beautiflow.chat.domain.ChatRoom;
import com.beautiflow.chat.domain.ChatRoomRead;
import com.beautiflow.chat.dto.chatRoomDto.ChatRoomSummaryRes;
import com.beautiflow.chat.dto.chatRoomDto.RoomCreateReq;
import com.beautiflow.chat.dto.chatRoomDto.RoomCreateRes;
import com.beautiflow.chat.repository.ChatMessageRepository;
import com.beautiflow.chat.repository.ChatRoomReadRepository;
import com.beautiflow.chat.repository.ChatRoomRepository;
import com.beautiflow.global.common.error.ChatRoomErrorCode;
import com.beautiflow.global.common.error.ShopErrorCode;
import com.beautiflow.global.common.error.UserErrorCode;
import com.beautiflow.global.common.exception.BeautiFlowException;
import com.beautiflow.global.common.sms.SmsService;
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
	private final ChatMessageRepository chatMessageRepository;
	private final ChatRoomReadRepository chatRoomReadRepository;
	private final SmsService smsService;

	private static final LocalDateTime MYSQL_MIN = LocalDateTime.of(1000, 1, 1, 0, 0);


	public RoomCreateRes createRoom(Long requesterId, RoomCreateReq roomCreateReq){
		Optional<ChatRoom> optional = chatRoomRepository
			.findByShopIdAndCustomerIdAndDesignerId(roomCreateReq.shopId(), roomCreateReq.customerId(), roomCreateReq.designerId());

		if (optional.isPresent()) {
			ChatRoom room = optional.get();

			// 재입장 처리
			User requester = userRepository.findById(requesterId)
				.orElseThrow(() -> new BeautiFlowException(UserErrorCode.USER_NOT_FOUND));
			room.reEnterBy(requester);

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

		smsService.sendNewContactAlert(designer.getContact());


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
			chatRoomReadRepository.deleteByChatRoomId(roomId);
			chatMessageRepository.deleteByChatRoomId(roomId);
			chatRoomRepository.delete(room);

		}
	}

	@Transactional(readOnly = true)
	public List<ChatRoomSummaryRes> getMyChatRooms(Long userId) {
		User me = userRepository.findById(userId)
			.orElseThrow(() -> new BeautiFlowException(UserErrorCode.USER_NOT_FOUND));

		List<ChatRoom> myRooms = chatRoomRepository.findMyActiveChatRooms(userId);


		return myRooms.stream().map(room -> {
			User opponent = room.getCustomer().equals(me) ? room.getDesigner() : room.getCustomer();

			// 가장 최근 메시지 조회
			ChatMessage lastMessage = chatMessageRepository.findTopByChatRoomOrderByCreatedTimeDesc(room).orElse(null);
			//마지막 읽은 시간
			LocalDateTime lastReadTime = chatRoomReadRepository
				.findByChatRoomAndUser(room,me)
				.map(ChatRoomRead::getLastReadTime)
				.filter(t -> t != null && !t.isBefore(MYSQL_MIN) && t.getYear() <= 9999)
				.orElse(MYSQL_MIN);


			int unreadCount = chatMessageRepository.countByChatRoomAndSenderNotAndCreatedTimeAfter(room, me, lastReadTime);

			return ChatRoomSummaryRes.of(room, opponent, lastMessage, unreadCount);
		}).toList();
	}

	public boolean isParticipant(Long userId, Long roomId) {
		return chatRoomRepository.findById(roomId)
			.filter(room -> room.getCustomer().getId().equals(userId) || room.getDesigner().getId().equals(userId))
			.isPresent();
	}


	public ChatRoom getOrCreateRoom(Shop shop, User customer, User designer) {
		return chatRoomRepository.findByShopIdAndCustomerIdAndDesignerId(shop.getId(), customer.getId(), designer.getId())
			.map(room -> {
				room.reEnterBy(customer);
				room.reEnterBy(designer);
				return room;
			})
			.orElseGet(() -> {
				ChatRoom newRoom = ChatRoom.builder()
					.shop(shop)
					.customer(customer)
					.designer(designer)
					.build();
				return chatRoomRepository.save(newRoom);
			});
	}


}