package com.blocksy.server.modules.report.controller;

import com.blocksy.server.common.api.ApiResponse;
import com.blocksy.server.modules.report.dto.ReportCreateRequest;
import com.blocksy.server.modules.report.dto.ReportResponse;
import com.blocksy.server.modules.report.service.ReportService;
import com.blocksy.server.security.SecurityUtils;
import com.blocksy.server.security.jwt.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Report", description = "举报接口")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping
    @Operation(summary = "提交举报")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<ReportResponse> create(@Valid @RequestBody ReportCreateRequest request) {
        AuthenticatedUser currentUser = SecurityUtils.getCurrentUserOrThrow();
        return ApiResponse.success(reportService.create(currentUser.userId(), request));
    }
}
