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
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
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

        Set<Long> existingIds = userStyle.getImages().stream()
            .map(UserStyleImage::getId)
            .collect(Collectors.toSet());

        for (Long id : imageIdsToDelete) {
            if (!existingIds.contains(id)) {
                throw new BeautiFlowException(UserErrorCode.USER_STYLE_IMAGE_NOT_FOUND);
            }
        }

        Iterator<UserStyleImage> iterator = userStyle.getImages().iterator();
        while (iterator.hasNext()) {
            UserStyleImage image = iterator.next();
            if (imageIdsToDelete.contains(image.getId())) {
                s3Service.deleteFile(image.getStoredFilePath());
                iterator.remove();
            }
        }
    }


}
