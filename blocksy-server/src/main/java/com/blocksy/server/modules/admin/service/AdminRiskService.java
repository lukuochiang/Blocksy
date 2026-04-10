package com.blocksy.server.modules.admin.service;

import com.blocksy.server.common.api.PageResponse;
import com.blocksy.server.modules.admin.dto.RiskAnomalyHandleRequest;
import com.blocksy.server.modules.admin.dto.RiskAnomalyResponse;
import com.blocksy.server.modules.admin.dto.RiskAppealHandleRequest;
import com.blocksy.server.modules.admin.dto.RiskAppealResponse;

public interface AdminRiskService {
    PageResponse<RiskAnomalyResponse> pageAnomalies(String processStatus, String level, String keyword, Integer page, Integer pageSize);

    RiskAnomalyResponse handleAnomaly(Long id, Long adminUserId, RiskAnomalyHandleRequest request);

    PageResponse<RiskAppealResponse> pageAppeals(String processStatus, String keyword, Integer page, Integer pageSize);

    RiskAppealResponse handleAppeal(Long id, Long adminUserId, RiskAppealHandleRequest request);
}
