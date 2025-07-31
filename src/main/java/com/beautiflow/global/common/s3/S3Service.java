package com.beautiflow.global.common.s3;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class S3Service {

  private final S3Client s3Client;

  @Value("${spring.cloud.aws.s3.bucket}")
  private String bucket;

  public S3UploadResult uploadFile(MultipartFile file, String dirName) {
    try {
      String originalFileName = file.getOriginalFilename();
      String fileKey = dirName + "/" + UUID.randomUUID() + "_" + originalFileName;

      PutObjectRequest putObjectRequest = PutObjectRequest.builder()
          .bucket(bucket)
          .key(fileKey)
          .contentType(file.getContentType())
          .contentLength(file.getSize())
          .build();

      // 파일 업로드
      s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

      // 업로드 된 파일의 URL
      GetUrlRequest getUrlRequest = GetUrlRequest.builder()
          .bucket(bucket)
          .key(fileKey)
          .build();

      URL fileUrl = s3Client.utilities().getUrl(getUrlRequest);

      return new S3UploadResult(fileUrl.toString(), fileKey);

    } catch (IOException e) {
      throw new RuntimeException("S3 파일 업로드에 실패했습니다.", e);
    }
  }

  public void deleteFile(String fileKey) {
    DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
        .bucket(bucket)
        .key(fileKey)
        .build();

    s3Client.deleteObject(deleteObjectRequest);
  }
}