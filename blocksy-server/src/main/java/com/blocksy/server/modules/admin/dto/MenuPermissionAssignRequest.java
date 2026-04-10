package com.blocksy.server.modules.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record MenuPermissionAssignRequest(
        @NotBlank(message = "roleCode 不能为空")
        String roleCode,
        @NotEmpty(message = "menuKeys 不能为空")
        List<String> menuKeys
) {
}
