package com.blocksy.server.modules.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserVerificationHandleRequest(
        @NotBlank(message = "processStatus 不能为空")
        String processStatus,
        @Size(max = 255, message = "reviewNote 不能超过 255 字符")
        String reviewNote
) {
}
