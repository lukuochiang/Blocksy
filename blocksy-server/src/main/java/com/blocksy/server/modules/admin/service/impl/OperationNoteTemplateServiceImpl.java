package com.blocksy.server.modules.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blocksy.server.common.exception.BusinessException;
import com.blocksy.server.modules.admin.dto.OperationNoteTemplateLogResponse;
import com.blocksy.server.modules.admin.dto.OperationNoteTemplateRuleResponse;
import com.blocksy.server.modules.admin.dto.OperationNoteTemplateResponse;
import com.blocksy.server.modules.admin.dto.OperationNoteTemplateSaveRequest;
import com.blocksy.server.modules.admin.entity.OperationNoteTemplateEntity;
import com.blocksy.server.modules.admin.entity.OperationNoteTemplateLogEntity;
import com.blocksy.server.modules.admin.mapper.OperationNoteTemplateLogMapper;
import com.blocksy.server.modules.admin.mapper.OperationNoteTemplateMapper;
import com.blocksy.server.modules.admin.service.OperationNoteTemplateService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class OperationNoteTemplateServiceImpl implements OperationNoteTemplateService {

    private final OperationNoteTemplateMapper mapper;
    private final OperationNoteTemplateLogMapper logMapper;

    public OperationNoteTemplateServiceImpl(OperationNoteTemplateMapper mapper, OperationNoteTemplateLogMapper logMapper) {
        this.mapper = mapper;
        this.logMapper = logMapper;
    }

    private static final Map<String, Set<String>> RULES = new LinkedHashMap<>();

    static {
        RULES.put("EVENT", Set.of("OFFLINE", "RESTORE", "DELETE"));
        RULES.put("LISTING", Set.of("APPROVE", "REJECT", "OFFLINE", "RESTORE", "DELETE"));
        RULES.put("REPORT", Set.of("RESOLVED", "REJECTED", "IN_PROGRESS"));
        RULES.put("USER", Set.of("BAN", "UNBAN", "MUTE", "UNMUTE"));
        RULES.put("POST", Set.of("OFFLINE", "RESTORE", "DELETE"));
        RULES.put("COMMENT", Set.of("OFFLINE", "RESTORE", "DELETE"));
    }

    @Override
    public List<OperationNoteTemplateRuleResponse> listRules() {
        return RULES.entrySet().stream()
                .map(entry -> new OperationNoteTemplateRuleResponse(entry.getKey(), entry.getValue().stream().sorted().toList()))
                .toList();
    }

    @Override
    public List<OperationNoteTemplateResponse> list(String module, String action, Integer status) {
        LambdaQueryWrapper<OperationNoteTemplateEntity> query = new LambdaQueryWrapper<OperationNoteTemplateEntity>()
                .orderByAsc(OperationNoteTemplateEntity::getSortNo)
                .orderByDesc(OperationNoteTemplateEntity::getCreatedAt)
                .last("LIMIT 500");
        if (StringUtils.hasText(module)) {
            query.eq(OperationNoteTemplateEntity::getModule, module.trim().toUpperCase());
        }
        if (StringUtils.hasText(action)) {
            query.eq(OperationNoteTemplateEntity::getAction, action.trim().toUpperCase());
        }
        if (status != null) {
            query.eq(OperationNoteTemplateEntity::getStatus, status);
        }
        return mapper.selectList(query).stream().map(this::toResponse).toList();
    }

    @Override
    public OperationNoteTemplateResponse create(Long operatorUserId, OperationNoteTemplateSaveRequest request) {
        String module = normalizeModule(request.module());
        String action = normalizeAction(module, request.action());
        String content = request.content().trim();
        LocalDateTime now = LocalDateTime.now();
        OperationNoteTemplateEntity entity = new OperationNoteTemplateEntity();
        entity.setModule(module);
        entity.setAction(action);
        entity.setContent(content);
        entity.setSortNo(request.sortNo() == null ? 0 : request.sortNo());
        entity.setStatus(request.status() == null ? 1 : request.status());
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        ensureNoDuplicate(null, module, action, content);
        mapper.insert(entity);
        insertLog(entity.getId(), operatorUserId, "CREATE", entity.getContent());
        return toResponse(entity);
    }

    @Override
    public OperationNoteTemplateResponse update(Long operatorUserId, Long id, OperationNoteTemplateSaveRequest request) {
        OperationNoteTemplateEntity entity = mapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("模板不存在");
        }
        String module = normalizeModule(request.module());
        String action = normalizeAction(module, request.action());
        String content = request.content().trim();
        ensureNoDuplicate(id, module, action, content);
        entity.setModule(module);
        entity.setAction(action);
        entity.setContent(content);
        entity.setSortNo(request.sortNo() == null ? 0 : request.sortNo());
        entity.setStatus(request.status() == null ? entity.getStatus() : request.status());
        entity.setUpdatedAt(LocalDateTime.now());
        mapper.updateById(entity);
        insertLog(entity.getId(), operatorUserId, "UPDATE", entity.getContent());
        return toResponse(entity);
    }

    @Override
    public void updateStatus(Long operatorUserId, Long id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("status 仅支持 0 或 1");
        }
        OperationNoteTemplateEntity entity = mapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("模板不存在");
        }
        entity.setStatus(status);
        entity.setUpdatedAt(LocalDateTime.now());
        mapper.updateById(entity);
        insertLog(entity.getId(), operatorUserId, status == 1 ? "ENABLE" : "DISABLE", entity.getContent());
    }

    @Override
    public List<OperationNoteTemplateLogResponse> listLogs(
            Long templateId,
            Long operatorUserId,
            String action,
            LocalDateTime startAt,
            LocalDateTime endAt
    ) {
        LambdaQueryWrapper<OperationNoteTemplateLogEntity> query = new LambdaQueryWrapper<OperationNoteTemplateLogEntity>()
                .eq(OperationNoteTemplateLogEntity::getStatus, 1)
                .orderByDesc(OperationNoteTemplateLogEntity::getCreatedAt)
                .last("LIMIT 500");
        if (templateId != null) {
            query.eq(OperationNoteTemplateLogEntity::getTemplateId, templateId);
        }
        if (operatorUserId != null) {
            query.eq(OperationNoteTemplateLogEntity::getOperatorUserId, operatorUserId);
        }
        if (StringUtils.hasText(action)) {
            query.eq(OperationNoteTemplateLogEntity::getAction, action.trim().toUpperCase());
        }
        if (startAt != null) {
            query.ge(OperationNoteTemplateLogEntity::getCreatedAt, startAt);
        }
        if (endAt != null) {
            query.le(OperationNoteTemplateLogEntity::getCreatedAt, endAt);
        }
        return logMapper.selectList(query).stream().map(log -> new OperationNoteTemplateLogResponse(
                log.getId(),
                log.getTemplateId(),
                log.getOperatorUserId(),
                log.getAction(),
                log.getNote(),
                log.getCreatedAt()
        )).toList();
    }

    private OperationNoteTemplateResponse toResponse(OperationNoteTemplateEntity entity) {
        return new OperationNoteTemplateResponse(
                entity.getId(),
                entity.getModule(),
                entity.getAction(),
                entity.getContent(),
                entity.getSortNo(),
                entity.getStatus()
        );
    }

    private void insertLog(Long templateId, Long operatorUserId, String action, String note) {
        LocalDateTime now = LocalDateTime.now();
        OperationNoteTemplateLogEntity log = new OperationNoteTemplateLogEntity();
        log.setTemplateId(templateId);
        log.setOperatorUserId(operatorUserId);
        log.setAction(action);
        log.setNote(note);
        log.setStatus(1);
        log.setCreatedAt(now);
        log.setUpdatedAt(now);
        logMapper.insert(log);
    }

    private String normalizeModule(String module) {
        String normalized = module == null ? "" : module.trim().toUpperCase();
        if (!StringUtils.hasText(normalized) || !RULES.containsKey(normalized)) {
            throw new BusinessException("不支持的 module，请从规则列表中选择");
        }
        return normalized;
    }

    private String normalizeAction(String module, String action) {
        String normalizedAction = action == null ? "" : action.trim().toUpperCase();
        if (!StringUtils.hasText(normalizedAction) || !RULES.get(module).contains(normalizedAction)) {
            throw new BusinessException("module/action 组合不合法，请从规则列表中选择");
        }
        return normalizedAction;
    }

    private void ensureNoDuplicate(Long currentId, String module, String action, String content) {
        LambdaQueryWrapper<OperationNoteTemplateEntity> query = new LambdaQueryWrapper<OperationNoteTemplateEntity>()
                .eq(OperationNoteTemplateEntity::getModule, module)
                .eq(OperationNoteTemplateEntity::getAction, action)
                .eq(OperationNoteTemplateEntity::getContent, content)
                .last("LIMIT 1");
        if (currentId != null) {
            query.ne(OperationNoteTemplateEntity::getId, currentId);
        }
        OperationNoteTemplateEntity duplicate = mapper.selectOne(query);
        if (duplicate != null) {
            throw new BusinessException("同一 module/action 下模板内容重复");
        }
    }
}
