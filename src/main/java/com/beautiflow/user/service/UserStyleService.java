package com.beautiflow.user.service;

import com.beautiflow.global.common.error.UserErrorCode;
import com.beautiflow.global.common.exception.BeautiFlowException;
import com.beautiflow.global.common.s3.S3Service;
import com.beautiflow.global.common.s3.S3UploadResult;
import com.beautiflow.user.domain.User;
import com.beautiflow.user.domain.UserStyle;
import com.beautiflow.user.domain.UserStyleImage;
import com.beautiflow.user.dto.UserStylePatchReq;
import com.beautiflow.user.dto.UserStyleReq;
import com.beautiflow.user.dto.UserStyleRes;
import com.beautiflow.user.repository.UserRepository;
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
    private final S3Service s3Service;

    @Transactional
    public UserStyleRes postUserStyle(Long userId, UserStyleReq req, List<MultipartFile> images) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BeautiFlowException(UserErrorCode.USER_NOT_FOUND));

        if (userStyleRepository.findByUserId(userId).isPresent()) {
            throw new BeautiFlowException(UserErrorCode.USER_STYLE_ALREADY_EXISTS);
        }

        String dir = String.format("users/%d/styles", userId);
        List<S3UploadResult> uploadResults = new ArrayList<>();

        for (MultipartFile image : images) {
            S3UploadResult result = s3Service.uploadFile(image, dir);
            uploadResults.add(result);
        }

        try {
            UserStyle userStyle = UserStyle.builder()
                    .user(user)
                    .description(req.description())
                    .createdAt(LocalDateTime.now())
                    .images(new ArrayList<>())
                    .build();

            for (S3UploadResult result : uploadResults) {
                userStyle.getImages().add(UserStyleImage.builder()
                        .imageUrl(result.imageUrl())
                        .userStyle(userStyle)
                        .build());
            }

            userStyleRepository.save(userStyle);
            return UserStyleRes.from(userStyle);

        } catch (Exception e) {
            for (S3UploadResult result : uploadResults) {
                s3Service.deleteFile(result.fileKey());
            }
            throw e;
        }
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

        if (patchReq.deleteImageUrls() != null && !patchReq.deleteImageUrls().isEmpty()) {
            Iterator<UserStyleImage> iterator = userStyle.getImages().iterator();
            while (iterator.hasNext()) {
                UserStyleImage img = iterator.next();
                if (patchReq.deleteImageUrls().contains(img.getImageUrl())) {
                    String fileKey = img.getImageUrl()
                            .substring(img.getImageUrl().indexOf("users/"));
                    s3Service.deleteFile(fileKey);
                    iterator.remove();
                }
            }
        }

        if (newImages != null && !newImages.isEmpty()) {
            String dir = String.format("users/%d/styles", userId);
            for (MultipartFile image : newImages) {
                S3UploadResult result = s3Service.uploadFile(image, dir);
                UserStyleImage newImage = UserStyleImage.builder()
                        .imageUrl(result.imageUrl())
                        .userStyle(userStyle)
                        .build();
                userStyle.getImages().add(newImage);
            }
        }

        return UserStyleRes.from(userStyle);
    }


}
