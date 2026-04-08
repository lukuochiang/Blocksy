package com.blocksy.server.infrastructure.minio.service;

import com.blocksy.server.common.exception.BusinessException;
import com.blocksy.server.common.enums.ResponseCodeEnum;
import com.blocksy.server.infrastructure.minio.properties.MinioProperties;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class MinioStorageService {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    public MinioStorageService(MinioClient minioClient, MinioProperties minioProperties) {
        this.minioClient = minioClient;
        this.minioProperties = minioProperties;
    }

    @PostConstruct
    public void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioProperties.getBucketName()).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioProperties.getBucketName()).build());
            }
        } catch (Exception ex) {
            throw new BusinessException(ResponseCodeEnum.INTERNAL_ERROR, "初始化 MinIO bucket 失败: " + ex.getMessage());
        }
    }

    public MinioUploadResult upload(MultipartFile file, String bizPath) {
        String cleanedBizPath = normalizeBizPath(bizPath);
        String objectKey = buildObjectKey(cleanedBizPath, file.getOriginalFilename());
        String contentType = StringUtils.defaultIfBlank(file.getContentType(), "application/octet-stream");
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(objectKey)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(contentType)
                            .build()
            );
            String url = StringUtils.removeEnd(minioProperties.getPublicUrl(), "/")
                    + "/" + minioProperties.getBucketName() + "/" + objectKey;
            return new MinioUploadResult(
                    minioProperties.getBucketName(),
                    objectKey,
                    url,
                    file.getSize(),
                    file.getOriginalFilename(),
                    contentType
            );
        } catch (Exception ex) {
            throw new BusinessException(ResponseCodeEnum.INTERNAL_ERROR, "文件上传失败: " + ex.getMessage());
        }
    }

    private String normalizeBizPath(String bizPath) {
        if (StringUtils.isBlank(bizPath)) {
            return "common";
        }
        return bizPath.trim().replace("\\", "/").replaceAll("^/+", "").replaceAll("/+$", "");
    }

    private String buildObjectKey(String bizPath, String originalFilename) {
        String extension = "";
        if (StringUtils.isNotBlank(originalFilename) && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        LocalDate date = LocalDate.now();
        return String.format(
                "%s/%04d/%02d/%02d/%s%s",
                bizPath,
                date.getYear(),
                date.getMonthValue(),
                date.getDayOfMonth(),
                UUID.randomUUID().toString().replace("-", ""),
                extension
        );
    }
}
