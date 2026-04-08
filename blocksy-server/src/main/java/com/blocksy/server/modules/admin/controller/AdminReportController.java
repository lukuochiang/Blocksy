package com.blocksy.server.modules.admin.controller;

import com.blocksy.server.common.api.ApiResponse;
import com.blocksy.server.modules.report.dto.AdminReportHandleRequest;
import com.blocksy.server.modules.report.dto.AdminReportResponse;
import com.blocksy.server.modules.report.service.ReportService;
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

import java.util.List;

@RestController
@RequestMapping("/api/admin/reports")
@Tag(name = "Admin-Report", description = "后台举报管理")
public class AdminReportController {

    private final ReportService reportService;
    private final AdminGuard adminGuard;

    public AdminReportController(ReportService reportService, AdminGuard adminGuard) {
        this.reportService = reportService;
        this.adminGuard = adminGuard;
    }

    @GetMapping
    @Operation(summary = "后台获取举报列表")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<List<AdminReportResponse>> list(@RequestParam(value = "processStatus", required = false) String processStatus) {
        adminGuard.requireAdmin();
        return ApiResponse.success(reportService.listForAdmin(processStatus));
    }

    @PostMapping("/{id}/handle")
    @Operation(summary = "后台处理举报")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<AdminReportResponse> handle(@PathVariable("id") Long id, @Valid @RequestBody AdminReportHandleRequest request) {
        AuthenticatedUser admin = adminGuard.requireAdmin();
        return ApiResponse.success(reportService.handleForAdmin(id, admin.userId(), request));
    }
}
