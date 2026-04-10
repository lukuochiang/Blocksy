package com.blocksy.server.modules.admin.service;

import com.blocksy.server.common.api.PageResponse;
import com.blocksy.server.modules.admin.dto.UserVerificationHandleRequest;
import com.blocksy.server.modules.admin.dto.UserVerificationResponse;

public interface AdminVerificationService {
    PageResponse<UserVerificationResponse> page(String processStatus, Long userId, Integer page, Integer pageSize);

    UserVerificationResponse handle(Long id, Long reviewerUserId, UserVerificationHandleRequest request);
}
