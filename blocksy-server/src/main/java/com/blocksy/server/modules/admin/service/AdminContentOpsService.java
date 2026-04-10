package com.blocksy.server.modules.admin.service;

import com.blocksy.server.common.api.PageResponse;
import com.blocksy.server.modules.admin.dto.CommunityEngagementResponse;
import com.blocksy.server.modules.admin.dto.ContentCategoryCreateRequest;
import com.blocksy.server.modules.admin.dto.ContentCategoryResponse;
import com.blocksy.server.modules.admin.dto.MediaAssetResponse;

public interface AdminContentOpsService {
    PageResponse<CommunityEngagementResponse> pageCommunityEngagement(String keyword, Integer page, Integer pageSize);

    PageResponse<ContentCategoryResponse> pageCategories(String module, String keyword, Integer page, Integer pageSize);

    ContentCategoryResponse createCategory(Long operatorUserId, ContentCategoryCreateRequest request);

    ContentCategoryResponse toggleCategory(Long id, Long operatorUserId, Boolean enabled);

    PageResponse<MediaAssetResponse> pagePostMedia(Long communityId, Long postId, Integer status, Integer page, Integer pageSize);

    MediaAssetResponse offlinePostMedia(Long mediaId, Long operatorUserId);
}
