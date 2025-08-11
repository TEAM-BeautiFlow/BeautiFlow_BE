package com.beautiflow.chat.dto;

public record RoomCreateReq(
	Long shopId,
	Long customerId,
	Long designerId
) {}
