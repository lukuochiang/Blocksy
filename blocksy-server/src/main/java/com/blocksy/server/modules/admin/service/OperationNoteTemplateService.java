package com.blocksy.server.modules.admin.service;

import com.blocksy.server.modules.admin.dto.OperationNoteTemplateResponse;
import com.blocksy.server.modules.admin.dto.OperationNoteTemplateSaveRequest;
import com.blocksy.server.modules.admin.dto.OperationNoteTemplateLogResponse;
import com.blocksy.server.modules.admin.dto.OperationNoteTemplateRuleResponse;

import java.time.LocalDateTime;

import java.util.List;

public interface OperationNoteTemplateService {
    List<OperationNoteTemplateRuleResponse> listRules();

    List<OperationNoteTemplateResponse> list(String module, String action, Integer status);

    OperationNoteTemplateResponse create(Long operatorUserId, OperationNoteTemplateSaveRequest request);

    OperationNoteTemplateResponse update(Long operatorUserId, Long id, OperationNoteTemplateSaveRequest request);

    void updateStatus(Long operatorUserId, Long id, Integer status);

    List<OperationNoteTemplateLogResponse> listLogs(
            Long templateId,
            Long operatorUserId,
            String action,
            LocalDateTime startAt,
            LocalDateTime endAt
    );
}
