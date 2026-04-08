package com.blocksy.server.modules.file.controller;

import com.blocksy.server.common.api.ApiResponse;
import com.blocksy.server.modules.file.service.FileService;
import com.blocksy.server.modules.file.vo.FileUploadResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@Tag(name = "File", description = "文件上传接口")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传文件到 MinIO", description = "上传单个文件到 bucket: blocksy-media；支持业务路径参数 bizPath")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "上传成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "上传失败")
    })
    public ApiResponse<FileUploadResultVO> upload(
            @Parameter(
                    description = "待上传文件",
                    required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE, schema = @Schema(type = "string", format = "binary"))
            )
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "业务路径，默认 common", example = "common")
            @RequestParam(value = "bizPath", required = false, defaultValue = "common") String bizPath
    ) {
        return ApiResponse.success(fileService.upload(file, bizPath));
    }
}
