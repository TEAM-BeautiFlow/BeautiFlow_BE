package com.beautiflow.shop.service;


import com.beautiflow.global.common.error.ShopErrorCode;
import com.beautiflow.global.common.error.UserErrorCode;
import com.beautiflow.global.common.exception.BeautiFlowException;
import com.beautiflow.global.domain.ApprovalStatus;
import com.beautiflow.global.domain.ShopRole;
import com.beautiflow.shop.converter.ShopConverter;
import com.beautiflow.shop.domain.Shop;
import com.beautiflow.shop.domain.ShopMember;
import com.beautiflow.shop.dto.ShopApplyRes;
import com.beautiflow.shop.dto.ShopExistsRes;
import com.beautiflow.shop.dto.ShopRegistrationReq;
import com.beautiflow.shop.dto.ShopRegistrationRes;
import com.beautiflow.shop.repository.ShopMemberRepository;
import com.beautiflow.shop.repository.ShopRepository;
import com.beautiflow.user.domain.User;
import com.beautiflow.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class ShopOnboardingService {

    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final ShopMemberRepository shopMemberRepository;

    public ShopRegistrationRes registerShop(Long userId, ShopRegistrationReq shopRegistrationReq) {

        String name = shopRegistrationReq.name();
        String address = shopRegistrationReq.address();
        String businessRegistrationNumber = shopRegistrationReq.businessRegistrationNumber();

        if (shopRepository.existsByBusinessRegistrationNumber(businessRegistrationNumber)) {
            throw new BeautiFlowException(ShopErrorCode.SHOP_ALREADY_REGISTERED);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BeautiFlowException(UserErrorCode.USER_NOT_FOUND));

        Shop shop = Shop.builder()
                .shopName(name)
                .address(address)
                .businessRegistrationNumber(businessRegistrationNumber)
                .build();
        shopRepository.save(shop);

        LocalDateTime now = LocalDateTime.now();

        ShopMember shopMember = ShopMember.builder()
                .shop(shop)
                .user(user)
                .role(ShopRole.OWNER)
                .status(ApprovalStatus.APPROVED)
                .appliedAt(now)
                .processedAt(now)
                .build();
        shopMemberRepository.save(shopMember);

        return ShopConverter.toShopRegistrationRes(shop, shopMember);

    }

    public ShopApplyRes ApplyToShop(Long userId, Long shopId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BeautiFlowException(UserErrorCode.USER_NOT_FOUND));
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new BeautiFlowException(ShopErrorCode.SHOP_NOT_FOUND));

        boolean isDuplicate = shopMemberRepository.existsByUserAndShop(user, shop);
        if (isDuplicate) {
            throw new BeautiFlowException(ShopErrorCode.ALREADY_SHOP_MEMBER);
        }

        LocalDateTime now = LocalDateTime.now();

        ShopMember shopMember = ShopMember.builder()
                .user(user)
                .shop(shop)
                .role(ShopRole.DESIGNER)
                .status(ApprovalStatus.PENDING)
                .appliedAt(now)
                .processedAt(now)
                .build();

        shopMemberRepository.save(shopMember);

        return ShopConverter.toShopApplyRes(shop, shopMember);

    }

    public ShopExistsRes IsShopExists(Long userId, String businessNumber) {

        userRepository.findById(userId).orElseThrow(()->new BeautiFlowException(UserErrorCode.USER_NOT_FOUND));
        var shopOpt = shopRepository.findByBusinessRegistrationNumber(businessNumber);

        if (shopOpt.isEmpty()) {
            return new ShopExistsRes(false, null);
        }

        var shop = shopOpt.get();
        return new ShopExistsRes(
                true,
                new ShopExistsRes.ShopDto(
                        shop.getId(),
                        shop.getShopName(),
                        shop.getAddress(),
                        shop.getBusinessRegistrationNumber()
                )
        );

    }


}
