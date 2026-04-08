package com.blocksy.server.modules.file.service;

import com.blocksy.server.modules.file.vo.FileUploadResultVO;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    FileUploadResultVO upload(MultipartFile file, String bizPath);
}
