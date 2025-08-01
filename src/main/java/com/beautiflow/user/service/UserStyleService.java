package com.beautiflow.user.service;

import com.beautiflow.global.common.error.ShopErrorCode;
import com.beautiflow.global.common.error.UserErrorCode;
import com.beautiflow.global.common.exception.BeautiFlowException;
import com.beautiflow.global.common.s3.S3Service;
import com.beautiflow.global.common.s3.S3UploadResult;
import com.beautiflow.shop.domain.Shop;
import com.beautiflow.shop.domain.ShopImage;
import com.beautiflow.user.domain.User;
import com.beautiflow.user.domain.UserStyle;
import com.beautiflow.user.domain.UserStyleImage;
import com.beautiflow.user.dto.UserStylePatchReq;
import com.beautiflow.user.dto.UserStyleReq;
import com.beautiflow.user.dto.UserStyleRes;
import com.beautiflow.user.repository.UserRepository;
import com.beautiflow.user.repository.UserStyleImageRepository;
import com.beautiflow.user.repository.UserStyleRepository;
import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserStyleService {

    private final UserRepository userRepository;
    private final UserStyleRepository userStyleRepository;
    private final UserStyleImageRepository userStyleImageRepository;
    private final S3Service s3Service;

    @Transactional
    public UserStyleRes postUserStyle(Long userId, UserStyleReq req, List<MultipartFile> images) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BeautiFlowException(UserErrorCode.USER_NOT_FOUND));

        if (userStyleRepository.findByUserId(userId).isPresent()) {
            throw new BeautiFlowException(UserErrorCode.USER_STYLE_ALREADY_EXISTS);
        }

        UserStyle userStyle = UserStyle.builder()
                .user(user)
                .description(req.description())
                .createdAt(LocalDateTime.now())
                .images(new ArrayList<>())
                .build();

        UserStyle savedStyle = userStyleRepository.save(userStyle);

        if (images != null && !images.isEmpty()) {
            uploadNewImages(userStyle, images);
        }

        return UserStyleRes.from(savedStyle);

    }

    public UserStyleRes getUserStyle(Long userId) {

        userRepository.findById(userId)
                .orElseThrow(() -> new BeautiFlowException(UserErrorCode.USER_NOT_FOUND));

        UserStyle userStyle = userStyleRepository.findByUserId(userId)
                .orElseThrow(() -> new BeautiFlowException(UserErrorCode.USER_STYLE_NOT_FOUND));

        return UserStyleRes.from(userStyle);
    }

    @Transactional
    public UserStyleRes patchUserStyle(Long userId, UserStylePatchReq patchReq,
            List<MultipartFile> newImages) {
        userRepository.findById(userId)
                .orElseThrow(() -> new BeautiFlowException(UserErrorCode.USER_NOT_FOUND));

        UserStyle userStyle = userStyleRepository.findByUserId(userId)
                .orElseThrow(() -> new BeautiFlowException(UserErrorCode.USER_STYLE_NOT_FOUND));

        if (patchReq.description() != null) {
            userStyle.updateDescription(patchReq.description());
        }

        if (patchReq.deleteImageIds() != null && !patchReq.deleteImageIds().isEmpty()) {
            deleteImages(userStyle, patchReq.deleteImageIds());
        }

        if (newImages != null && !newImages.isEmpty()) {
            uploadNewImages(userStyle, newImages);
        }

        return UserStyleRes.from(userStyle);


    }

    private void uploadNewImages(UserStyle userStyle, List<MultipartFile> newImages) {

        Long userId = userStyle.getUser().getId();
        String dir = String.format("users/%d/styles", userId);

        for (MultipartFile file : newImages) {
            S3UploadResult result = s3Service.uploadFile(file, dir);
            System.out.println(result);
            UserStyleImage userStyleImage = UserStyleImage.builder()
                    .userStyle(userStyle)
                    .originalFileName(file.getOriginalFilename())
                    .storedFilePath(result.fileKey())
                    .imageUrl(result.imageUrl())
                    .build();

            try {
                userStyle.getImages().add(userStyleImage);
                userStyleImageRepository.save(userStyleImage);

            } catch (Exception e) {
                s3Service.deleteFile(result.fileKey());
            }
        }
    }

    private void deleteImages(UserStyle userStyle, List<Long> imageIdsToDelete) {
        // 삭제할 이미지 ID에 대해 유효성 검사 수행
        for (Long imageId : imageIdsToDelete) {
            // 해당 ID 이미지 찾기
            UserStyleImage imageToRemove = userStyle.getImages().stream()
                    .filter(shopImage -> shopImage.getId().equals(imageId))
                    .findFirst()
                    .orElseThrow(() -> new BeautiFlowException(UserErrorCode.USER_STYLE_NOT_FOUND));

            // S3에 있는 실제 파일 삭제
            s3Service.deleteFile(imageToRemove.getStoredFilePath());
            userStyle.getImages().remove(imageToRemove);
        }
    }


}
