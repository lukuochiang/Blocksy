package com.blocksy.server.modules.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blocksy.server.common.api.PageResponse;
import com.blocksy.server.common.exception.BusinessException;
import com.blocksy.server.modules.admin.dto.RiskAnomalyHandleRequest;
import com.blocksy.server.modules.admin.dto.RiskAnomalyResponse;
import com.blocksy.server.modules.admin.dto.RiskAppealHandleRequest;
import com.blocksy.server.modules.admin.dto.RiskAppealResponse;
import com.blocksy.server.modules.admin.entity.AdminOperationLogEntity;
import com.blocksy.server.modules.admin.entity.RiskAnomalyEntity;
import com.blocksy.server.modules.admin.entity.RiskAppealEntity;
import com.blocksy.server.modules.admin.mapper.AdminOperationLogMapper;
import com.blocksy.server.modules.admin.mapper.RiskAnomalyMapper;
import com.blocksy.server.modules.admin.mapper.RiskAppealMapper;
import com.blocksy.server.modules.admin.service.AdminRiskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class AdminRiskServiceImpl implements AdminRiskService {

    private final RiskAnomalyMapper riskAnomalyMapper;
    private final RiskAppealMapper riskAppealMapper;
    private final AdminOperationLogMapper adminOperationLogMapper;

    public AdminRiskServiceImpl(
            RiskAnomalyMapper riskAnomalyMapper,
            RiskAppealMapper riskAppealMapper,
            AdminOperationLogMapper adminOperationLogMapper
    ) {
        this.riskAnomalyMapper = riskAnomalyMapper;
        this.riskAppealMapper = riskAppealMapper;
        this.adminOperationLogMapper = adminOperationLogMapper;
    }

    @Override
    public PageResponse<RiskAnomalyResponse> pageAnomalies(String processStatus, String level, String keyword, Integer page, Integer pageSize) {
        int currentPage = resolvePage(page);
        int size = resolvePageSize(pageSize, 100);
        LambdaQueryWrapper<RiskAnomalyEntity> countQuery = new LambdaQueryWrapper<RiskAnomalyEntity>()
                .eq(RiskAnomalyEntity::getStatus, 1);
        if (processStatus != null && !processStatus.isBlank()) {
            countQuery.eq(RiskAnomalyEntity::getProcessStatus, processStatus.trim().toUpperCase());
        }
        if (level != null && !level.isBlank()) {
            countQuery.eq(RiskAnomalyEntity::getLevel, level.trim().toUpperCase());
        }
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            countQuery.and(w -> w.like(RiskAnomalyEntity::getAnomalyType, kw)
                    .or()
                    .like(RiskAnomalyEntity::getDetails, kw));
        }
        long total = riskAnomalyMapper.selectCount(countQuery);
        if (total == 0) {
            return new PageResponse<>(currentPage, size, 0L, Collections.emptyList());
        }
        LambdaQueryWrapper<RiskAnomalyEntity> pageQuery = new LambdaQueryWrapper<RiskAnomalyEntity>()
                .eq(RiskAnomalyEntity::getStatus, 1)
                .orderByDesc(RiskAnomalyEntity::getCreatedAt)
                .last("LIMIT " + size + " OFFSET " + ((currentPage - 1) * size));
        if (processStatus != null && !processStatus.isBlank()) {
            pageQuery.eq(RiskAnomalyEntity::getProcessStatus, processStatus.trim().toUpperCase());
        }
        if (level != null && !level.isBlank()) {
            pageQuery.eq(RiskAnomalyEntity::getLevel, level.trim().toUpperCase());
        }
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            pageQuery.and(w -> w.like(RiskAnomalyEntity::getAnomalyType, kw)
                    .or()
                    .like(RiskAnomalyEntity::getDetails, kw));
        }
        List<RiskAnomalyResponse> items = riskAnomalyMapper.selectList(pageQuery).stream().map(this::toAnomalyResponse).toList();
        return new PageResponse<>(currentPage, size, total, items);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RiskAnomalyResponse handleAnomaly(Long id, Long adminUserId, RiskAnomalyHandleRequest request) {
        RiskAnomalyEntity entity = riskAnomalyMapper.selectById(id);
        if (entity == null || entity.getStatus() == null || entity.getStatus() != 1) {
            throw new BusinessException("异常记录不存在");
        }
        entity.setProcessStatus(request.processStatus().trim().toUpperCase());
        entity.setHandleNote(request.handleNote());
        entity.setAssigneeUserId(adminUserId);
        entity.setProcessedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        riskAnomalyMapper.updateById(entity);
        appendOperationLog("HANDLE_ANOMALY", adminUserId, "RISK_ANOMALY", id, "status=" + entity.getProcessStatus());
        return toAnomalyResponse(entity);
    }

    @Override
    public PageResponse<RiskAppealResponse> pageAppeals(String processStatus, String keyword, Integer page, Integer pageSize) {
        int currentPage = resolvePage(page);
        int size = resolvePageSize(pageSize, 100);
        LambdaQueryWrapper<RiskAppealEntity> countQuery = new LambdaQueryWrapper<RiskAppealEntity>()
                .eq(RiskAppealEntity::getStatus, 1);
        if (processStatus != null && !processStatus.isBlank()) {
            countQuery.eq(RiskAppealEntity::getProcessStatus, processStatus.trim().toUpperCase());
        }
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            countQuery.and(w -> w.like(RiskAppealEntity::getAppealReason, kw)
                    .or()
                    .like(RiskAppealEntity::getAppealContent, kw));
        }
        long total = riskAppealMapper.selectCount(countQuery);
        if (total == 0) {
            return new PageResponse<>(currentPage, size, 0L, Collections.emptyList());
        }
        LambdaQueryWrapper<RiskAppealEntity> pageQuery = new LambdaQueryWrapper<RiskAppealEntity>()
                .eq(RiskAppealEntity::getStatus, 1)
                .orderByDesc(RiskAppealEntity::getCreatedAt)
                .last("LIMIT " + size + " OFFSET " + ((currentPage - 1) * size));
        if (processStatus != null && !processStatus.isBlank()) {
            pageQuery.eq(RiskAppealEntity::getProcessStatus, processStatus.trim().toUpperCase());
        }
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            pageQuery.and(w -> w.like(RiskAppealEntity::getAppealReason, kw)
                    .or()
                    .like(RiskAppealEntity::getAppealContent, kw));
        }
        List<RiskAppealResponse> items = riskAppealMapper.selectList(pageQuery).stream().map(this::toAppealResponse).toList();
        return new PageResponse<>(currentPage, size, total, items);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RiskAppealResponse handleAppeal(Long id, Long adminUserId, RiskAppealHandleRequest request) {
        RiskAppealEntity entity = riskAppealMapper.selectById(id);
        if (entity == null || entity.getStatus() == null || entity.getStatus() != 1) {
            throw new BusinessException("申诉记录不存在");
        }
        entity.setProcessStatus(request.processStatus().trim().toUpperCase());
        entity.setResultNote(request.resultNote());
        entity.setAssigneeUserId(adminUserId);
        entity.setProcessedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        riskAppealMapper.updateById(entity);
        appendOperationLog("HANDLE_APPEAL", adminUserId, "RISK_APPEAL", id, "status=" + entity.getProcessStatus());
        return toAppealResponse(entity);
    }

    private RiskAnomalyResponse toAnomalyResponse(RiskAnomalyEntity row) {
        return new RiskAnomalyResponse(
                row.getId(),
                row.getUserId(),
                row.getAnomalyType(),
                row.getLevel(),
                row.getDetails(),
                row.getProcessStatus(),
                row.getHandleNote(),
                row.getAssigneeUserId(),
                row.getProcessedAt(),
                row.getCreatedAt()
        );
    }

    private RiskAppealResponse toAppealResponse(RiskAppealEntity row) {
        return new RiskAppealResponse(
                row.getId(),
                row.getUserId(),
                row.getPunishLogId(),
                row.getAppealReason(),
                row.getAppealContent(),
                row.getProcessStatus(),
                row.getResultNote(),
                row.getAssigneeUserId(),
                row.getProcessedAt(),
                row.getCreatedAt()
        );
    }

    private int resolvePage(Integer page) {
        return page == null || page < 1 ? 1 : page;
    }

    private int resolvePageSize(Integer pageSize, int max) {
        if (pageSize == null || pageSize < 1) {
            return 10;
        }
        return Math.min(pageSize, max);
    }

    private void appendOperationLog(String action, Long operatorUserId, String targetType, Long targetId, String details) {
        LocalDateTime now = LocalDateTime.now();
        AdminOperationLogEntity log = new AdminOperationLogEntity();
        log.setModule("RISK");
        log.setAction(action);
        log.setOperatorUserId(operatorUserId);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setDetails(details);
        log.setStatus(1);
        log.setCreatedAt(now);
        log.setUpdatedAt(now);
        adminOperationLogMapper.insert(log);
    }
}
