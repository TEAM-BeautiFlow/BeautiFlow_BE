package com.beautiflow.chat.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.beautiflow.chat.domain.MessageTemplate;
import com.beautiflow.chat.dto.ChatMessageSendReq;
import com.beautiflow.chat.dto.MessageTemplateCreateReq;
import com.beautiflow.chat.dto.MessageTemplateRes;
import com.beautiflow.chat.dto.MessageTemplateUpdateReq;
import com.beautiflow.chat.repository.MessageTemplateRepository;
import com.beautiflow.global.common.error.TemplateErrorCode;
import com.beautiflow.global.common.error.UserErrorCode;
import com.beautiflow.global.common.exception.BeautiFlowException;
import com.beautiflow.global.domain.SenderType;
import com.beautiflow.user.domain.User;
import com.beautiflow.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class MessageTemplateService {

	private final MessageTemplateRepository templateRepository;
	private final UserRepository userRepository;

	public void create(Long ownerId, MessageTemplateCreateReq req) {
		User owner = userRepository.findById(ownerId)
			.orElseThrow(() -> new BeautiFlowException(UserErrorCode.USER_NOT_FOUND));

		MessageTemplate template = MessageTemplate.builder()
			.name(req.name())
			.sendTiming(req.sendTiming())
			.daysOffset(req.daysOffset())
			.content(req.content())
			.targetGroup(req.targetGroup())
			.isActive(true)
			.owner(owner)
			.build();

		templateRepository.save(template);
	}

	public List<MessageTemplateRes> getMyTemplates(Long ownerId) {
		User owner = userRepository.findById(ownerId)
			.orElseThrow(() -> new BeautiFlowException(UserErrorCode.USER_NOT_FOUND));

		return templateRepository.findByOwner(owner).stream()
			.map(MessageTemplateRes::from)
			.collect(Collectors.toList());
	}

	public void update(Long templateId, MessageTemplateUpdateReq req) {
		MessageTemplate template = templateRepository.findById(templateId)
			.orElseThrow(() -> new BeautiFlowException(TemplateErrorCode.Template_NOT_FOUND));
		template.update(req);
	}

	public void delete(Long templateId) {
		templateRepository.deleteById(templateId);
	}

	@Transactional(readOnly = true)
	public ChatMessageSendReq toSendReq(Long templateId, Long roomId, Long senderId) {

		MessageTemplate messageTemplate = templateRepository.findById(templateId)
			.orElseThrow(() -> new BeautiFlowException(TemplateErrorCode.Template_NOT_FOUND));
		if (!messageTemplate.isActive()) throw new BeautiFlowException(TemplateErrorCode.INACTIVE_TEMPLATE);

		return new ChatMessageSendReq(
			roomId,
			senderId,
			SenderType.STAFF,
			messageTemplate.getContent(),
			null
		);
	}
}

