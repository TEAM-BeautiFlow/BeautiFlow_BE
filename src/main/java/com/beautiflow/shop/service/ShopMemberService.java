package com.beautiflow.shop.service;

import com.beautiflow.global.common.error.ShopErrorCode;
import com.beautiflow.global.common.error.UserErrorCode;
import com.beautiflow.global.common.exception.BeautiFlowException;
import com.beautiflow.global.common.s3.S3Service;
import com.beautiflow.global.common.s3.S3UploadResult;
import com.beautiflow.global.common.security.annotation.AuthCheck;
import com.beautiflow.global.domain.ShopRole;
import com.beautiflow.shop.domain.Shop;
import com.beautiflow.shop.domain.ShopImage;
import com.beautiflow.shop.domain.ShopMember;
import com.beautiflow.shop.dto.ShopMemberInfoReq;
import com.beautiflow.shop.dto.ShopMemberInfoRes;
import com.beautiflow.shop.repository.ShopMemberRepository;
import com.beautiflow.shop.repository.ShopRepository;
import com.beautiflow.user.domain.User;
import com.beautiflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ShopMemberService {

    private final S3Service s3Service;
    private final ShopMemberRepository shopMemberRepository;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;

    @Transactional
    public ShopMemberInfoRes patchInfo(Long shopId, Long designerId,
            ShopMemberInfoReq shopMemberInfoReq, MultipartFile image) {

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new BeautiFlowException(ShopErrorCode.SHOP_NOT_FOUND));

        User user = userRepository.findById(designerId)
                .orElseThrow(() -> new BeautiFlowException(UserErrorCode.USER_NOT_FOUND));

        ShopMember shopMember = shopMemberRepository.findByUserIdAndShopId(user.getId(), shopId)
                .orElseThrow(() ->
                        new BeautiFlowException(ShopErrorCode.SHOP_MEMBER_NOT_FOUND));

        if (shopMemberInfoReq.patchImage()) {
            deleteImages(shopMember);

            if (image != null && !image.isEmpty()) {
                uploadNewImages(shopMember, image);
            }
        }

        shopMember.updateIntro(shopMemberInfoReq.intro());

        return ShopMemberInfoRes.builder()
                .userId(user.getId())
                .shopId(shop.getId())
                .memberId(shopMember.getId())
                .intro(shopMember.getIntro())
                .imageUrl(shopMember.getImageUrl())
                .build();
    }

    public ShopMemberInfoRes getInfo(Long shopId, Long designerId) {

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new BeautiFlowException(ShopErrorCode.SHOP_NOT_FOUND));

        User user = userRepository.findById(designerId)
                .orElseThrow(() -> new BeautiFlowException(UserErrorCode.USER_NOT_FOUND));

        ShopMember shopMember = shopMemberRepository.findByUserIdAndShopId(user.getId(), shopId)
                .orElseThrow(() ->
                        new BeautiFlowException(ShopErrorCode.SHOP_MEMBER_NOT_FOUND));

        return ShopMemberInfoRes.builder()
                .userId(user.getId())
                .shopId(shop.getId())
                .memberId(shopMember.getId())
                .intro(shopMember.getIntro())
                .imageUrl(shopMember.getImageUrl())
                .build();
    }


    private void deleteImages(ShopMember shopMember) {

        if (shopMember.getStoredFilePath() == null) {
            return;
        }
        s3Service.deleteFile(shopMember.getStoredFilePath());
        shopMember.clearImageInfo();
    }

    private void uploadNewImages(ShopMember shopMember, MultipartFile image) {
        String dir = String.format("shopMembers/%d/infos", shopMember.getId());
        S3UploadResult result = s3Service.uploadFile(image, dir);

        shopMember.updateImageInfo(result.imageUrl(), image.getOriginalFilename(),
                result.fileKey());
    }


}
