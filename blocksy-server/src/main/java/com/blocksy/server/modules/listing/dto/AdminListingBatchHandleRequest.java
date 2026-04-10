package com.blocksy.server.modules.listing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(description = "后台分类信息批量处理请求")
public record AdminListingBatchHandleRequest(
        @Schema(description = "分类信息 ID 列表", example = "[1001,1002,1003]")
        @NotEmpty(message = "listingIds 不能为空")
        List<Long> listingIds,
        @Schema(description = "处理动作：APPROVE/REJECT/OFFLINE/RESTORE/DELETE", example = "APPROVE")
        @NotBlank(message = "action 不能为空")
        String action,
        @Schema(description = "处理备注", example = "批量审核通过")
        String note
) {
}
