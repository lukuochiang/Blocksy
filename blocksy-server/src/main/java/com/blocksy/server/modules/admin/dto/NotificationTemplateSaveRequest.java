package com.blocksy.server.modules.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NotificationTemplateSaveRequest(
        @NotBlank(message = "module 不能为空")
        @Size(max = 64, message = "module 不能超过 64 字符")
        String module,
        @NotBlank(message = "triggerCode 不能为空")
        @Size(max = 64, message = "triggerCode 不能超过 64 字符")
        String triggerCode,
        @NotBlank(message = "titleTemplate 不能为空")
        @Size(max = 120, message = "titleTemplate 不能超过 120 字符")
        String titleTemplate,
        @NotBlank(message = "contentTemplate 不能为空")
        @Size(max = 500, message = "contentTemplate 不能超过 500 字符")
        String contentTemplate,
        Boolean enabled
) {
}
