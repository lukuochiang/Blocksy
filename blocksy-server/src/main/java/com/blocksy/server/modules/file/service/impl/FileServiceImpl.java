package com.blocksy.server.modules.file.service.impl;

import com.blocksy.server.common.enums.ResponseCodeEnum;
import com.blocksy.server.common.exception.BusinessException;
import com.blocksy.server.infrastructure.minio.service.MinioStorageService;
import com.blocksy.server.infrastructure.minio.service.MinioUploadResult;
import com.blocksy.server.modules.file.service.FileService;
import com.blocksy.server.modules.file.vo.FileUploadResultVO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileServiceImpl implements FileService {

    private final MinioStorageService minioStorageService;

    public FileServiceImpl(MinioStorageService minioStorageService) {
        this.minioStorageService = minioStorageService;
    }

    @Override
    public FileUploadResultVO upload(MultipartFile file, String bizPath) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResponseCodeEnum.BAD_REQUEST, "上传文件不能为空");
        }
        if (file.getSize() <= 0) {
            throw new BusinessException(ResponseCodeEnum.BAD_REQUEST, "上传文件大小不能为 0");
        }
        MinioUploadResult result = minioStorageService.upload(file, bizPath);
        return new FileUploadResultVO(
                result.bucket(),
                result.objectKey(),
                result.url(),
                result.size(),
                result.originalFilename(),
                result.contentType()
        );
    }
}
