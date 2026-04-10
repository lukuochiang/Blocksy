package com.blocksy.server.modules.admin.service;

import com.blocksy.server.common.api.PageResponse;
import com.blocksy.server.modules.admin.dto.CommunityNoticeCreateRequest;
import com.blocksy.server.modules.admin.dto.CommunityNoticeResponse;

public interface AdminCommunityNoticeService {
    PageResponse<CommunityNoticeResponse> page(Long communityId, Integer status, String keyword, Integer page, Integer pageSize);

    CommunityNoticeResponse create(Long operatorUserId, CommunityNoticeCreateRequest request);

    CommunityNoticeResponse revoke(Long noticeId, Long operatorUserId);
}
