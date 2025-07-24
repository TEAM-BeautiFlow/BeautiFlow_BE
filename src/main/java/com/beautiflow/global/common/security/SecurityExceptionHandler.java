package com.beautiflow.global.common.security;

import com.beautiflow.global.common.ApiResponse;
import com.beautiflow.global.common.error.UserErrorCode;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
//컨트롤러 진입 시 발생한 예외처리, 로그인이 되지 않은 경우 등
@RestControllerAdvice
public class SecurityExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<?>> handleAuthenticationException(
    ) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.createFail(UserErrorCode.LOGIN_REQUIRED));
    }
}
