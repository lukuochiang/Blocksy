package com.blocksy.server.modules.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blocksy.server.common.api.PageResponse;
import com.blocksy.server.common.exception.BusinessException;
import com.blocksy.server.modules.admin.dto.UserVerificationHandleRequest;
import com.blocksy.server.modules.admin.dto.UserVerificationResponse;
import com.blocksy.server.modules.admin.entity.AdminOperationLogEntity;
import com.blocksy.server.modules.admin.entity.UserVerificationApplicationEntity;
import com.blocksy.server.modules.admin.mapper.AdminOperationLogMapper;
import com.blocksy.server.modules.admin.mapper.UserVerificationApplicationMapper;
import com.blocksy.server.modules.admin.service.AdminVerificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class AdminVerificationServiceImpl implements AdminVerificationService {

    private final UserVerificationApplicationMapper verificationMapper;
    private final AdminOperationLogMapper adminOperationLogMapper;

    public AdminVerificationServiceImpl(
            UserVerificationApplicationMapper verificationMapper,
            AdminOperationLogMapper adminOperationLogMapper
    ) {
        this.verificationMapper = verificationMapper;
        this.adminOperationLogMapper = adminOperationLogMapper;
    }

    @Override
    public PageResponse<UserVerificationResponse> page(String processStatus, Long userId, Integer page, Integer pageSize) {
        int currentPage = page == null || page < 1 ? 1 : page;
        int size = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 100);
        LambdaQueryWrapper<UserVerificationApplicationEntity> countQuery = new LambdaQueryWrapper<UserVerificationApplicationEntity>()
                .eq(UserVerificationApplicationEntity::getStatus, 1);
        if (processStatus != null && !processStatus.isBlank()) {
            countQuery.eq(UserVerificationApplicationEntity::getProcessStatus, processStatus.trim().toUpperCase());
        }
        if (userId != null) {
            countQuery.eq(UserVerificationApplicationEntity::getUserId, userId);
        }
        long total = verificationMapper.selectCount(countQuery);
        if (total == 0) {
            return new PageResponse<>(currentPage, size, 0L, Collections.emptyList());
        }
        LambdaQueryWrapper<UserVerificationApplicationEntity> pageQuery = new LambdaQueryWrapper<UserVerificationApplicationEntity>()
                .eq(UserVerificationApplicationEntity::getStatus, 1)
                .orderByDesc(UserVerificationApplicationEntity::getCreatedAt)
                .last("LIMIT " + size + " OFFSET " + ((currentPage - 1) * size));
        if (processStatus != null && !processStatus.isBlank()) {
            pageQuery.eq(UserVerificationApplicationEntity::getProcessStatus, processStatus.trim().toUpperCase());
        }
        if (userId != null) {
            pageQuery.eq(UserVerificationApplicationEntity::getUserId, userId);
        }
        List<UserVerificationResponse> items = verificationMapper.selectList(pageQuery).stream().map(this::toResponse).toList();
        return new PageResponse<>(currentPage, size, total, items);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVerificationResponse handle(Long id, Long reviewerUserId, UserVerificationHandleRequest request) {
        UserVerificationApplicationEntity row = verificationMapper.selectById(id);
        if (row == null || row.getStatus() == null || row.getStatus() != 1) {
            throw new BusinessException("认证申请不存在");
        }
        String status = request.processStatus().trim().toUpperCase();
        row.setProcessStatus(status);
        row.setReviewNote(request.reviewNote());
        row.setReviewerUserId(reviewerUserId);
        row.setReviewedAt(LocalDateTime.now());
        row.setUpdatedAt(LocalDateTime.now());
        verificationMapper.updateById(row);

        AdminOperationLogEntity log = new AdminOperationLogEntity();
        log.setModule("USER_VERIFICATION");
        log.setAction("HANDLE");
        log.setOperatorUserId(reviewerUserId);
        log.setTargetType("VERIFICATION");
        log.setTargetId(id);
        log.setDetails("status=" + status);
        log.setStatus(1);
        log.setCreatedAt(LocalDateTime.now());
        log.setUpdatedAt(LocalDateTime.now());
        adminOperationLogMapper.insert(log);
        return toResponse(row);
    }

    private UserVerificationResponse toResponse(UserVerificationApplicationEntity row) {
        return new UserVerificationResponse(
                row.getId(),
                row.getUserId(),
                row.getVerifyType(),
                row.getRealName(),
                row.getIdCardMask(),
                row.getMaterialUrls(),
                row.getProcessStatus(),
                row.getReviewNote(),
                row.getReviewerUserId(),
                row.getReviewedAt(),
                row.getCreatedAt()
        );
    }
}
