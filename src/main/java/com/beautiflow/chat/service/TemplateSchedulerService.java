
package com.beautiflow.chat.service;

import java.time.LocalDate;

import java.util.List;


import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.beautiflow.chat.domain.ChatRoom;
import com.beautiflow.chat.domain.MessageTemplate;
import com.beautiflow.chat.domain.TargetGroup;
import com.beautiflow.chat.dto.ChatMessageSendReq;
import com.beautiflow.chat.repository.MessageTemplateRepository;
import com.beautiflow.global.domain.SenderType;
import com.beautiflow.reservation.domain.Reservation;
import com.beautiflow.reservation.repository.ReservationRepository;
import com.beautiflow.user.domain.User;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
public class TemplateSchedulerService {


}
