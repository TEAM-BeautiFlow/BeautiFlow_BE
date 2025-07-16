package com.beautiflow.chat.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.beautiflow.chat.domain.MessageTemplate;
import com.beautiflow.chat.dto.MessageTemplateCreateReq;
import com.beautiflow.chat.repository.MessageTemplateRepository;
import com.beautiflow.global.common.error.UserErrorCode;
import com.beautiflow.global.common.exception.BeautiFlowException;
import com.beautiflow.user.domain.User;
import com.beautiflow.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class MessageTemplateService {

	private final MessageTemplateRepository templateRepository;
	private final UserRepository userRepository;

	public void createTemplate(Long ownerId, MessageTemplateCreateReq req) {
		User owner = userRepository.findById(ownerId)
			.orElseThrow(() -> new BeautiFlowException(UserErrorCode.USER_NOT_FOUND));

		MessageTemplate template = MessageTemplate.builder()
			.name(req.name())
			.sendTiming(req.sendTiming())
			.daysOffset(req.daysOffset())
			.content(req.content())
			.targetGroup(req.targetGroup())
			.isActive(req.isActive())
			.owner(owner)
			.build();

		templateRepository.save(template);
	}
}