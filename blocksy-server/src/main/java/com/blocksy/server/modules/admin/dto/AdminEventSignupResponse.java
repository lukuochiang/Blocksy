package com.blocksy.server.modules.admin.dto;

import java.time.LocalDateTime;

public record AdminEventSignupResponse(
        Long signupId,
        Long eventId,
        String eventTitle,
        Long communityId,
        Long userId,
        String remark,
        Integer status,
        LocalDateTime signupAt
) {
}
