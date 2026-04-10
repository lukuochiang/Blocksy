package com.blocksy.server.modules.admin.dto;

public record CommunityEngagementResponse(
        Long communityId,
        String communityName,
        Long memberCount,
        Long postCount,
        Long commentCount,
        Long activeScore
) {
}
