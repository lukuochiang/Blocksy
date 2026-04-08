package com.blocksy.server.modules.event.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record EventSignupRequest(
        @Schema(description = "报名备注", example = "两人参加")
        String remark
) {
}
