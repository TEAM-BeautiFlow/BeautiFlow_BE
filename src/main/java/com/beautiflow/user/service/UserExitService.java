package com.beautiflow.user.service;

import com.beautiflow.global.common.error.UserErrorCode;
import com.beautiflow.global.common.exception.BeautiFlowException;
import com.beautiflow.global.common.util.RedisTokenUtil;
import com.beautiflow.user.domain.User;
import com.beautiflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class UserExitService {

    private final UserRepository userRepository;
    private final RedisTokenUtil redisTokenUtil;

    //refreshToken 삭제
    public void logout(Long userId) {

        userRepository.findById(userId)
                .orElseThrow(() -> new BeautiFlowException(UserErrorCode.USER_NOT_FOUND));

        String redisKey = "refresh:" + userId;
        redisTokenUtil.deleteValues(redisKey);

    }

    @Transactional
    public void delete(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BeautiFlowException(UserErrorCode.USER_NOT_FOUND));
        if (user.isDeleted()) {
            throw new BeautiFlowException(UserErrorCode.USER_ALREADY_DELETED);
        }else{
            userRepository.delete(user);
        }

    }

}
