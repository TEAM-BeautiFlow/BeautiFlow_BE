package com.beautiflow.chat.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.beautiflow.chat.domain.MessageTemplate;
import com.beautiflow.chat.dto.messageTemplateDto.MessageTemplateCreateReq;
import com.beautiflow.chat.dto.messageTemplateDto.MessageTemplateSummaryRes;
import com.beautiflow.chat.dto.messageTemplateDto.MessageTemplateUpdateReq;
import com.beautiflow.chat.repository.MessageTemplateRepository;
import com.beautiflow.global.common.error.TemplateErrorCode;
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


	public void updateTemplate(Long ownerId, Long templateId, MessageTemplateUpdateReq req) {
		MessageTemplate template = templateRepository.findById(templateId)
			.orElseThrow(() -> new BeautiFlowException(TemplateErrorCode.TEMPLATE_NOT_FOUND));

		if (!template.getOwner().getId().equals(ownerId)) {
			throw new BeautiFlowException(TemplateErrorCode.NO_TEMPLATE_PERMISSION);
		}

		template.update(req);
	}

	public void deleteTemplate(Long ownerId, Long templateId) {
		MessageTemplate template = templateRepository.findById(templateId)
			.orElseThrow(() -> new BeautiFlowException(TemplateErrorCode.TEMPLATE_NOT_FOUND));

		if (!template.getOwner().getId().equals(ownerId)) {
			throw new BeautiFlowException(TemplateErrorCode.NO_TEMPLATE_PERMISSION);
		}

		templateRepository.delete(template);
	}

	@Transactional(readOnly = true)
	public List<MessageTemplateSummaryRes> getTemplates(Long userId) {
		User owner = userRepository.findById(userId)
			.orElseThrow(() -> new BeautiFlowException(UserErrorCode.USER_NOT_FOUND));

		List<MessageTemplate> templates = templateRepository.findByOwner(owner);

		return templates.stream()
			.map(template -> MessageTemplateSummaryRes.builder()
				.id(template.getId())
				.isActive(template.isActive())
				.name(template.getName())
				.sendTiming(template.getSendTiming())
				.daysOffset(template.getDaysOffset())
				.targetGroup(template.getTargetGroup())
				.build()
			).toList();
	}

}