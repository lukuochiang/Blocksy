package com.blocksy.server.modules.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blocksy.server.common.exception.BusinessException;
import com.blocksy.server.modules.post.entity.PostEntity;
import com.blocksy.server.modules.post.mapper.PostMapper;
import com.blocksy.server.modules.report.dto.AdminReportHandleRequest;
import com.blocksy.server.modules.report.dto.AdminReportResponse;
import com.blocksy.server.modules.report.dto.ReportCreateRequest;
import com.blocksy.server.modules.report.dto.ReportResponse;
import com.blocksy.server.modules.report.entity.ReportEntity;
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
    private final PostMapper postMapper;
    private final UserService userService;

    public ReportServiceImpl(ReportMapper reportMapper, PostMapper postMapper, UserService userService) {
        this.reportMapper = reportMapper;
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
        String action = request.action().toUpperCase();
        if (!"RESOLVED".equals(action) && !"REJECTED".equals(action)) {
            throw new BusinessException("action 仅支持 RESOLVED 或 REJECTED");
        }
        entity.setProcessStatus(action);
        entity.setHandlerUserId(handlerUserId);
        entity.setHandledAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        if (StringUtils.hasText(request.note())) {
            entity.setDescription((entity.getDescription() == null ? "" : entity.getDescription() + " | ") + "ADMIN: " + request.note());
        }
        reportMapper.updateById(entity);

        if ("RESOLVED".equals(action) && Boolean.TRUE.equals(request.banTargetUser()) && "POST".equalsIgnoreCase(entity.getTargetType())) {
            PostEntity post = postMapper.selectById(entity.getTargetId());
            if (post != null) {
                userService.banUser(post.getUserId());
            }
        }
        return toAdminResponse(entity);
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
                entity.getHandledAt(),
                entity.getCreatedAt()
        );
    }
}
