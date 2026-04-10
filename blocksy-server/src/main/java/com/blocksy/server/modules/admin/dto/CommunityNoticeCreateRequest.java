package com.blocksy.server.modules.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CommunityNoticeCreateRequest(
        @NotNull(message = "communityId 不能为空")
        Long communityId,
        @NotBlank(message = "title 不能为空")
        @Size(max = 120, message = "title 不能超过 120 字符")
        String title,
        @NotBlank(message = "content 不能为空")
        @Size(max = 1000, message = "content 不能超过 1000 字符")
        String content
) {
}
