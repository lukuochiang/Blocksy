package com.blocksy.server.modules.listing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AdminListingBatchRetryRequest(
        @NotEmpty(message = "failedListingIds 不能为空")
        List<Long> failedListingIds,
        @NotBlank(message = "action 不能为空")
        String action,
        @Size(max = 500, message = "note 不能超过 500 字符")
        String note
) {
}
