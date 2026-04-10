package com.blocksy.server.modules.listing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record AdminListingHandleRequest(
        @Schema(description = "处理动作：APPROVE/REJECT/OFFLINE/RESTORE/DELETE", example = "APPROVE")
        @NotBlank(message = "action 不能为空")
        String action,
        @Schema(description = "处理备注", example = "违规广告，已下架")
        String note
) {
}
