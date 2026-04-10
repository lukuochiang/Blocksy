package com.blocksy.server.modules.admin.controller;

import com.blocksy.server.common.api.ApiResponse;
import com.blocksy.server.modules.admin.dto.OperationNoteTemplateLogResponse;
import com.blocksy.server.modules.admin.dto.OperationNoteTemplateRuleResponse;
import com.blocksy.server.modules.admin.dto.OperationNoteTemplateResponse;
import com.blocksy.server.modules.admin.dto.OperationNoteTemplateSaveRequest;
import com.blocksy.server.modules.admin.service.OperationNoteTemplateService;
import com.blocksy.server.security.jwt.AuthenticatedUser;
import com.blocksy.server.security.service.AdminGuard;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/operation-note-templates")
@Tag(name = "Admin-Operation-Note-Template", description = "后台操作备注模板管理")
public class AdminOperationNoteTemplateController {

    private final OperationNoteTemplateService service;
    private final AdminGuard adminGuard;

    public AdminOperationNoteTemplateController(OperationNoteTemplateService service, AdminGuard adminGuard) {
        this.service = service;
        this.adminGuard = adminGuard;
    }

    @GetMapping
    @Operation(summary = "获取操作备注模板列表")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<List<OperationNoteTemplateResponse>> list(
            @RequestParam(value = "module", required = false) String module,
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "status", required = false) Integer status
    ) {
        adminGuard.requireAdmin();
        return ApiResponse.success(service.list(module, action, status));
    }

    @GetMapping("/rules")
    @Operation(summary = "获取模板模块与动作绑定规则")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<List<OperationNoteTemplateRuleResponse>> rules() {
        adminGuard.requireAdmin();
        return ApiResponse.success(service.listRules());
    }

    @PostMapping
    @Operation(summary = "新增操作备注模板")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<OperationNoteTemplateResponse> create(@Valid @RequestBody OperationNoteTemplateSaveRequest request) {
        AuthenticatedUser operator = adminGuard.requireTemplateManager();
        return ApiResponse.success(service.create(operator.userId(), request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新操作备注模板")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<OperationNoteTemplateResponse> update(
            @PathVariable("id") Long id,
            @Valid @RequestBody OperationNoteTemplateSaveRequest request
    ) {
        AuthenticatedUser operator = adminGuard.requireTemplateManager();
        return ApiResponse.success(service.update(operator.userId(), id, request));
    }

    @PostMapping("/{id}/status")
    @Operation(summary = "启用/禁用操作备注模板")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<Boolean> updateStatus(@PathVariable("id") Long id, @RequestParam("status") Integer status) {
        AuthenticatedUser operator = adminGuard.requireTemplateManager();
        service.updateStatus(operator.userId(), id, status);
        return ApiResponse.success(Boolean.TRUE);
    }

    @GetMapping("/logs")
    @Operation(summary = "获取操作备注模板日志")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<List<OperationNoteTemplateLogResponse>> logs(
            @RequestParam(value = "templateId", required = false) Long templateId,
            @RequestParam(value = "operatorUserId", required = false) Long operatorUserId,
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "startAt", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startAt,
            @RequestParam(value = "endAt", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endAt
    ) {
        adminGuard.requireAdmin();
        return ApiResponse.success(service.listLogs(templateId, operatorUserId, action, startAt, endAt));
    }

    @GetMapping("/{id}/logs")
    @Operation(summary = "获取单个模板操作日志")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<List<OperationNoteTemplateLogResponse>> logsById(@PathVariable("id") Long id) {
        adminGuard.requireAdmin();
        return ApiResponse.success(service.listLogs(id, null, null, null, null));
    }

    @GetMapping("/permission")
    @Operation(summary = "获取当前账号模板管理权限")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<Boolean> permission() {
        try {
            adminGuard.requireTemplateManager();
            return ApiResponse.success(Boolean.TRUE);
        } catch (Exception ex) {
            return ApiResponse.success(Boolean.FALSE);
        }
    }
}
