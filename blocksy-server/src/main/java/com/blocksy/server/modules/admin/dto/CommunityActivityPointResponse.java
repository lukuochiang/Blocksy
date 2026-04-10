package com.blocksy.server.modules.admin.dto;

public record CommunityActivityPointResponse(
        Long communityId,
        String communityName,
        Long postCount,
        Long commentCount,
        Long reportCount
) {
}
