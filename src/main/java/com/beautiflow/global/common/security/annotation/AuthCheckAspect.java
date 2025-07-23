package com.beautiflow.global.common.security.annotation;

import com.beautiflow.global.common.error.ShopErrorCode;
import com.beautiflow.global.common.error.UserErrorCode;
import com.beautiflow.global.common.exception.BeautiFlowException;
import com.beautiflow.global.common.security.CustomOAuth2User;
import com.beautiflow.global.domain.ApprovalStatus;
import com.beautiflow.shop.domain.ShopMember;
import com.beautiflow.shop.repository.ShopMemberRepository;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@RequiredArgsConstructor
@Component
public class AuthCheckAspect {

    private final ShopMemberRepository shopMemberRepository;

    @Around("@annotation(authCheck)")
    public Object validateShopRole(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {

        // 현재 인증 정보에서 userId 가져오기
        Long userId = getCurrentUserId();

        //@PathVariable 기반으로 shopId 추출
        Long shopId = extractShopId(joinPoint);

        //ShopMember 조회
        ShopMember shopMember = shopMemberRepository.findByUserIdAndShopId(userId, shopId)
                .orElseThrow(() -> new BeautiFlowException(ShopErrorCode.UNAUTHORIZED_SHOP_ACCESS));

        // 권한 체크
        boolean hasRequiredRole = Arrays.stream(authCheck.value())
                .anyMatch(requiredRole -> requiredRole == shopMember.getRole());


        if (shopMember.getStatus().equals(ApprovalStatus.PENDING)) {
            throw new BeautiFlowException(ShopErrorCode.SHOP_MEMBER_NOT_APPROVED);
        }

        if (shopMember.getStatus().equals(ApprovalStatus.REJECTED)) {
            throw new BeautiFlowException(ShopErrorCode.UNAUTHORIZED_SHOP_ACCESS);
        }

        if (!hasRequiredRole) {
            throw new BeautiFlowException(ShopErrorCode.ACCESS_DENIED_SHOP_ROLE);
        }


        return joinPoint.proceed();
    }

    //현재 로그인된 사용자 id 추출
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new BeautiFlowException(UserErrorCode.UNAUTHORIZED_ACCESS);
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof CustomOAuth2User customUser) {
            return customUser.getUserId();
        }

        throw new BeautiFlowException(UserErrorCode.UNAUTHORIZED_ACCESS);
    }

    //파라미터 인자 중 shopId를 찾아 반환
    private Long extractShopId(ProceedingJoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = methodSignature.getParameterNames();
        Object[] args = joinPoint.getArgs();
        Parameter[] parameters = methodSignature.getMethod().getParameters();

        for (int i = 0; i < parameters.length; i++) {
            if (parameterNames[i].equals("shopId") && args[i] instanceof Long) {
                return (Long) args[i];
            }
        }

        throw new BeautiFlowException(ShopErrorCode.SHOP_NOT_FOUND);
    }
}