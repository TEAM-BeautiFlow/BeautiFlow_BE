package com.beautiflow.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.beautiflow.chat.domain.MessageTemplate;

public interface MessageTemplateRepository extends JpaRepository<MessageTemplate, Long> {
}