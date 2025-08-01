package com.beautiflow.global.common.util;

import com.beautiflow.global.common.error.CommonErrorCode;
import com.beautiflow.global.common.exception.BeautiFlowException;
import io.lettuce.core.RedisCommandTimeoutException;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisTokenUtil {

    private final RedisTemplate<String, String> redisTemplate;

    public void setValues(String key, String value, Duration duration) {
        try {
            redisTemplate.opsForValue().set(key, value, duration);
        } catch (RedisConnectionFailureException | RedisCommandTimeoutException e) {
            throw new BeautiFlowException(CommonErrorCode.REDIS_CONNECTION_FAILED);
        }
    }

    public String getValues(String key) {
        try {
            ValueOperations<String, String> values = redisTemplate.opsForValue();
            return values.get(key);
        } catch (RedisConnectionFailureException | RedisCommandTimeoutException e) {
            throw new BeautiFlowException(CommonErrorCode.REDIS_CONNECTION_FAILED);
        }
    }


    public void deleteValues(String key) {
        try {
            redisTemplate.delete(key);
        } catch (RedisConnectionFailureException | RedisCommandTimeoutException e) {
            throw new BeautiFlowException(CommonErrorCode.REDIS_CONNECTION_FAILED);
        }
    }


}
