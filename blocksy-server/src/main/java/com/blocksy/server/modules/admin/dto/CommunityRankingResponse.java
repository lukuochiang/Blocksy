package com.blocksy.server.modules.admin.dto;

public record CommunityRankingResponse(
        Long communityId,
        String communityName,
        Long postCount,
        Long commentCount,
        Long reportCount,
        Double score
) {
}
