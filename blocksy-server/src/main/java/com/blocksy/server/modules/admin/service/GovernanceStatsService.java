package com.blocksy.server.modules.admin.service;

import com.blocksy.server.modules.admin.dto.GovernanceStatsResponse;

public interface GovernanceStatsService {
    GovernanceStatsResponse getOverview();
}
