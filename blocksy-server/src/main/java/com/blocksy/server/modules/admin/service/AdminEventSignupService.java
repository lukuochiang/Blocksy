package com.blocksy.server.modules.admin.service;

import com.blocksy.server.common.api.PageResponse;
import com.blocksy.server.modules.admin.dto.AdminEventSignupResponse;

public interface AdminEventSignupService {
    PageResponse<AdminEventSignupResponse> page(Long eventId, Long communityId, Long userId, Integer page, Integer pageSize);
}
