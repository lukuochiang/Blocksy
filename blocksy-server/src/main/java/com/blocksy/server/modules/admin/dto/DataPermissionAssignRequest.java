package com.blocksy.server.modules.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record DataPermissionAssignRequest(
        @NotBlank(message = "roleCode 不能为空")
        String roleCode,
        @NotBlank(message = "dataScope 不能为空")
        String dataScope,
        @NotEmpty(message = "dataValues 不能为空")
        List<String> dataValues
) {
}
