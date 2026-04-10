package com.blocksy.server.modules.admin.controller;

import com.blocksy.server.common.api.ApiResponse;
import com.blocksy.server.common.api.PageResponse;
import com.blocksy.server.modules.admin.dto.RiskAnomalyHandleRequest;
import com.blocksy.server.modules.admin.dto.RiskAnomalyResponse;
import com.blocksy.server.modules.admin.dto.RiskAppealHandleRequest;
import com.blocksy.server.modules.admin.dto.RiskAppealResponse;
import com.blocksy.server.modules.admin.service.AdminRiskService;
import com.blocksy.server.security.jwt.AuthenticatedUser;
import com.blocksy.server.security.service.AdminGuard;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/risk")
@Tag(name = "Admin-Risk", description = "后台风控与申诉管理")
public class AdminRiskController {

    private final AdminRiskService adminRiskService;
    private final AdminGuard adminGuard;

    public AdminRiskController(AdminRiskService adminRiskService, AdminGuard adminGuard) {
        this.adminRiskService = adminRiskService;
        this.adminGuard = adminGuard;
    }

    @GetMapping("/anomalies")
    @Operation(summary = "异常行为列表（分页/筛选）")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<PageResponse<RiskAnomalyResponse>> anomalies(
            @RequestParam(value = "processStatus", required = false) String processStatus,
            @RequestParam(value = "level", required = false) String level,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize
    ) {
        adminGuard.requireAdmin();
        return ApiResponse.success(adminRiskService.pageAnomalies(processStatus, level, keyword, page, pageSize));
    }

    @PostMapping("/anomalies/{id}/handle")
    @Operation(summary = "处理异常行为")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<RiskAnomalyResponse> handleAnomaly(@PathVariable("id") Long id, @Valid @RequestBody RiskAnomalyHandleRequest request) {
        AuthenticatedUser admin = adminGuard.requireAdmin();
        return ApiResponse.success(adminRiskService.handleAnomaly(id, admin.userId(), request));
    }

    @GetMapping("/appeals")
    @Operation(summary = "申诉列表（分页/筛选）")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<PageResponse<RiskAppealResponse>> appeals(
            @RequestParam(value = "processStatus", required = false) String processStatus,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize
    ) {
        adminGuard.requireAdmin();
        return ApiResponse.success(adminRiskService.pageAppeals(processStatus, keyword, page, pageSize));
    }

    @PostMapping("/appeals/{id}/handle")
    @Operation(summary = "处理申诉")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<RiskAppealResponse> handleAppeal(@PathVariable("id") Long id, @Valid @RequestBody RiskAppealHandleRequest request) {
        AuthenticatedUser admin = adminGuard.requireAdmin();
        return ApiResponse.success(adminRiskService.handleAppeal(id, admin.userId(), request));
    }
}
