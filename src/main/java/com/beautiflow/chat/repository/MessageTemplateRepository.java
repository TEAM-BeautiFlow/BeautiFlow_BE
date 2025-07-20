package com.beautiflow.chat.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.beautiflow.chat.domain.MessageTemplate;
import com.beautiflow.user.domain.User;

public interface MessageTemplateRepository extends JpaRepository<MessageTemplate, Long> {

	List<MessageTemplate> findByOwnerAndIsActiveTrue(User owner);
	List<MessageTemplate> findByIsActiveTrue();

}