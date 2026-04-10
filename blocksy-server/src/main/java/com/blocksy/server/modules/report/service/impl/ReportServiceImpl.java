package com.blocksy.server.modules.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blocksy.server.common.exception.BusinessException;
import com.blocksy.server.modules.post.entity.PostEntity;
import com.blocksy.server.modules.post.mapper.PostMapper;
import com.blocksy.server.modules.report.dto.AdminReportHandleRequest;
import com.blocksy.server.modules.report.dto.AdminReportHandleLogResponse;
import com.blocksy.server.modules.report.dto.AdminReportResponse;
import com.blocksy.server.modules.report.dto.AdminReportBatchHandleRequest;
import com.blocksy.server.modules.report.dto.AdminReportBatchHandleResponse;
import com.blocksy.server.modules.report.dto.AdminReportBatchItemResult;
import com.blocksy.server.modules.report.dto.AdminReportBatchRetryRequest;
import com.blocksy.server.modules.report.dto.ReportCreateRequest;
import com.blocksy.server.modules.report.dto.ReportResponse;
import com.blocksy.server.modules.report.entity.ReportEntity;
import com.blocksy.server.modules.report.entity.ReportHandleLogEntity;
import com.blocksy.server.modules.report.mapper.ReportHandleLogMapper;
import com.blocksy.server.modules.report.mapper.ReportMapper;
import com.blocksy.server.modules.report.service.ReportService;
import com.blocksy.server.modules.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReportServiceImpl implements ReportService {

    private final ReportMapper reportMapper;
    private final ReportHandleLogMapper reportHandleLogMapper;
    private final PostMapper postMapper;
    private final UserService userService;

    public ReportServiceImpl(
            ReportMapper reportMapper,
            ReportHandleLogMapper reportHandleLogMapper,
            PostMapper postMapper,
            UserService userService
    ) {
        this.reportMapper = reportMapper;
        this.reportHandleLogMapper = reportHandleLogMapper;
        this.postMapper = postMapper;
        this.userService = userService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReportResponse create(Long reporterUserId, ReportCreateRequest request) {
        LocalDateTime now = LocalDateTime.now();
        ReportEntity entity = new ReportEntity();
        entity.setReporterUserId(reporterUserId);
        entity.setTargetType(request.targetType());
        entity.setTargetId(request.targetId());
        entity.setReason(request.reason());
        entity.setDescription(request.description());
        entity.setProcessStatus("PENDING");
        entity.setStatus(1);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        reportMapper.insert(entity);
        return new ReportResponse(
                entity.getId(),
                entity.getTargetType(),
                entity.getTargetId(),
                entity.getReason(),
                entity.getProcessStatus(),
                entity.getCreatedAt()
        );
    }

    @Override
    public List<AdminReportResponse> listForAdmin(String processStatus) {
        LambdaQueryWrapper<ReportEntity> query = new LambdaQueryWrapper<ReportEntity>()
                .eq(ReportEntity::getStatus, 1)
                .orderByDesc(ReportEntity::getCreatedAt)
                .last("LIMIT 200");
        if (StringUtils.hasText(processStatus)) {
            query.eq(ReportEntity::getProcessStatus, processStatus.toUpperCase());
        }
        return reportMapper.selectList(query).stream().map(this::toAdminResponse).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminReportResponse handleForAdmin(Long reportId, Long handlerUserId, AdminReportHandleRequest request) {
        ReportEntity entity = reportMapper.selectById(reportId);
        if (entity == null || entity.getStatus() == null || entity.getStatus() != 1) {
            throw new BusinessException("举报记录不存在");
        }
        if (!"PENDING".equalsIgnoreCase(entity.getProcessStatus())) {
            throw new BusinessException("该举报已处理，不能重复处理");
        }
        String action = request.action().toUpperCase();
        if (!"RESOLVED".equals(action) && !"REJECTED".equals(action)) {
            throw new BusinessException("action 仅支持 RESOLVED 或 REJECTED");
        }
        entity.setProcessStatus(action);
        entity.setHandlerUserId(handlerUserId);
        entity.setHandlerNote(request.note());
        entity.setHandledAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        reportMapper.updateById(entity);

        boolean banTargetUser = Boolean.TRUE.equals(request.banTargetUser());
        if ("RESOLVED".equals(action) && banTargetUser && "POST".equalsIgnoreCase(entity.getTargetType())) {
            PostEntity post = postMapper.selectById(entity.getTargetId());
            if (post != null) {
                userService.banUser(post.getUserId());
            }
        }
        insertHandleLog(entity.getId(), handlerUserId, action, request.note(), banTargetUser);
        return toAdminResponse(entity);
    }

    @Override
    public AdminReportBatchHandleResponse batchHandleForAdmin(Long handlerUserId, AdminReportBatchHandleRequest request) {
        List<Long> successIds = new java.util.ArrayList<>();
        List<Long> skippedIds = new java.util.ArrayList<>();
        List<AdminReportBatchItemResult> failedItems = new java.util.ArrayList<>();

        for (Long reportId : request.reportIds()) {
            if (reportId == null) {
                failedItems.add(new AdminReportBatchItemResult(null, "reportId 不能为空"));
                continue;
            }
            ReportEntity entity = reportMapper.selectById(reportId);
            if (entity == null || entity.getStatus() == null || entity.getStatus() != 1) {
                failedItems.add(new AdminReportBatchItemResult(reportId, "举报记录不存在"));
                continue;
            }
            if (!"PENDING".equalsIgnoreCase(entity.getProcessStatus())) {
                skippedIds.add(reportId);
                continue;
            }
            try {
                handleForAdmin(reportId, handlerUserId, new AdminReportHandleRequest(
                        request.action(),
                        request.note(),
                        request.banTargetUser()
                ));
                successIds.add(reportId);
            } catch (Exception ex) {
                failedItems.add(new AdminReportBatchItemResult(reportId, ex.getMessage()));
            }
        }
        return new AdminReportBatchHandleResponse(
                request.reportIds().size(),
                successIds.size(),
                successIds,
                skippedIds,
                failedItems
        );
    }

    @Override
    public AdminReportBatchHandleResponse batchRetryForAdmin(Long handlerUserId, AdminReportBatchRetryRequest request) {
        return batchHandleForAdmin(
                handlerUserId,
                new AdminReportBatchHandleRequest(
                        request.failedReportIds(),
                        request.action(),
                        request.note(),
                        request.banTargetUser()
                )
        );
    }

    @Override
    public String exportBatchHandleResultCsv(AdminReportBatchHandleResponse response) {
        StringBuilder csv = new StringBuilder("status,report_id,message\n");
        if (response.successIds() != null) {
            for (Long reportId : response.successIds()) {
                csv.append("SUCCESS,").append(reportId == null ? "" : reportId).append(",ok\n");
            }
        }
        if (response.skippedIds() != null) {
            for (Long reportId : response.skippedIds()) {
                csv.append("SKIPPED,").append(reportId == null ? "" : reportId).append(",not_pending\n");
            }
        }
        if (response.failedItems() != null) {
            for (AdminReportBatchItemResult failed : response.failedItems()) {
                csv.append("FAILED,")
                        .append(failed.reportId() == null ? "" : failed.reportId())
                        .append(',')
                        .append(escapeCsv(failed.message()))
                        .append('\n');
            }
        }
        return csv.toString();
    }

    @Override
    public List<AdminReportHandleLogResponse> listHandleLogsForAdmin(Long reportId) {
        LambdaQueryWrapper<ReportHandleLogEntity> query = new LambdaQueryWrapper<ReportHandleLogEntity>()
                .eq(ReportHandleLogEntity::getReportId, reportId)
                .eq(ReportHandleLogEntity::getStatus, 1)
                .orderByDesc(ReportHandleLogEntity::getCreatedAt)
                .last("LIMIT 100");
        return reportHandleLogMapper.selectList(query).stream()
                .map(this::toHandleLogResponse)
                .toList();
    }

    private void insertHandleLog(Long reportId, Long operatorUserId, String action, String note, boolean banTargetUser) {
        LocalDateTime now = LocalDateTime.now();
        ReportHandleLogEntity logEntity = new ReportHandleLogEntity();
        logEntity.setReportId(reportId);
        logEntity.setOperatorUserId(operatorUserId);
        logEntity.setAction(action);
        logEntity.setNote(note);
        logEntity.setBanTargetUser(banTargetUser);
        logEntity.setStatus(1);
        logEntity.setCreatedAt(now);
        logEntity.setUpdatedAt(now);
        reportHandleLogMapper.insert(logEntity);
    }

    private AdminReportResponse toAdminResponse(ReportEntity entity) {
        return new AdminReportResponse(
                entity.getId(),
                entity.getReporterUserId(),
                entity.getTargetType(),
                entity.getTargetId(),
                entity.getReason(),
                entity.getProcessStatus(),
                entity.getHandlerUserId(),
                entity.getHandlerNote(),
                entity.getHandledAt(),
                entity.getCreatedAt()
        );
    }

    private AdminReportHandleLogResponse toHandleLogResponse(ReportHandleLogEntity entity) {
        return new AdminReportHandleLogResponse(
                entity.getId(),
                entity.getReportId(),
                entity.getOperatorUserId(),
                entity.getAction(),
                entity.getNote(),
                entity.getBanTargetUser(),
                entity.getCreatedAt()
        );
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\"")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }
}
