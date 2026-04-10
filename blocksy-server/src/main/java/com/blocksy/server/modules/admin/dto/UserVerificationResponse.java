package com.blocksy.server.modules.admin.dto;

import java.time.LocalDateTime;

public record UserVerificationResponse(
        Long id,
        Long userId,
        String verifyType,
        String realName,
        String idCardMask,
        String materialUrls,
        String processStatus,
        String reviewNote,
        Long reviewerUserId,
        LocalDateTime reviewedAt,
        LocalDateTime createdAt
) {
}
