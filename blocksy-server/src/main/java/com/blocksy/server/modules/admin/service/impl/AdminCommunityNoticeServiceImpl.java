package com.blocksy.server.modules.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blocksy.server.common.api.PageResponse;
import com.blocksy.server.common.exception.BusinessException;
import com.blocksy.server.modules.admin.dto.CommunityNoticeCreateRequest;
import com.blocksy.server.modules.admin.dto.CommunityNoticeResponse;
import com.blocksy.server.modules.admin.entity.AdminOperationLogEntity;
import com.blocksy.server.modules.admin.entity.CommunityNoticeEntity;
import com.blocksy.server.modules.admin.mapper.AdminOperationLogMapper;
import com.blocksy.server.modules.admin.mapper.CommunityNoticeMapper;
import com.blocksy.server.modules.admin.service.AdminCommunityNoticeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class AdminCommunityNoticeServiceImpl implements AdminCommunityNoticeService {

    private final CommunityNoticeMapper noticeMapper;
    private final AdminOperationLogMapper adminOperationLogMapper;

    public AdminCommunityNoticeServiceImpl(CommunityNoticeMapper noticeMapper, AdminOperationLogMapper adminOperationLogMapper) {
        this.noticeMapper = noticeMapper;
        this.adminOperationLogMapper = adminOperationLogMapper;
    }

    @Override
    public PageResponse<CommunityNoticeResponse> page(Long communityId, Integer status, String keyword, Integer page, Integer pageSize) {
        int currentPage = page == null || page < 1 ? 1 : page;
        int size = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 100);
        LambdaQueryWrapper<CommunityNoticeEntity> countQuery = new LambdaQueryWrapper<>();
        if (communityId != null) {
            countQuery.eq(CommunityNoticeEntity::getCommunityId, communityId);
        }
        if (status != null) {
            countQuery.eq(CommunityNoticeEntity::getStatus, status);
        }
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            countQuery.and(w -> w.like(CommunityNoticeEntity::getTitle, kw)
                    .or()
                    .like(CommunityNoticeEntity::getContent, kw));
        }
        long total = noticeMapper.selectCount(countQuery);
        if (total == 0) {
            return new PageResponse<>(currentPage, size, 0L, Collections.emptyList());
        }
        LambdaQueryWrapper<CommunityNoticeEntity> pageQuery = new LambdaQueryWrapper<CommunityNoticeEntity>()
                .orderByDesc(CommunityNoticeEntity::getCreatedAt)
                .last("LIMIT " + size + " OFFSET " + ((currentPage - 1) * size));
        if (communityId != null) {
            pageQuery.eq(CommunityNoticeEntity::getCommunityId, communityId);
        }
        if (status != null) {
            pageQuery.eq(CommunityNoticeEntity::getStatus, status);
        }
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            pageQuery.and(w -> w.like(CommunityNoticeEntity::getTitle, kw)
                    .or()
                    .like(CommunityNoticeEntity::getContent, kw));
        }
        List<CommunityNoticeResponse> items = noticeMapper.selectList(pageQuery).stream().map(this::toResponse).toList();
        return new PageResponse<>(currentPage, size, total, items);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommunityNoticeResponse create(Long operatorUserId, CommunityNoticeCreateRequest request) {
        LocalDateTime now = LocalDateTime.now();
        CommunityNoticeEntity row = new CommunityNoticeEntity();
        row.setCommunityId(request.communityId());
        row.setTitle(request.title().trim());
        row.setContent(request.content().trim());
        row.setStatus(1);
        row.setCreatedBy(operatorUserId);
        row.setCreatedAt(now);
        row.setUpdatedAt(now);
        noticeMapper.insert(row);
        appendLog("CREATE", operatorUserId, row.getId(), "communityId=" + request.communityId());
        return toResponse(row);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommunityNoticeResponse revoke(Long noticeId, Long operatorUserId) {
        CommunityNoticeEntity row = noticeMapper.selectById(noticeId);
        if (row == null) {
            throw new BusinessException("公告不存在");
        }
        if (row.getStatus() != null && row.getStatus() == 0) {
            return toResponse(row);
        }
        row.setStatus(0);
        row.setRevokedBy(operatorUserId);
        row.setRevokedAt(LocalDateTime.now());
        row.setUpdatedAt(LocalDateTime.now());
        noticeMapper.updateById(row);
        appendLog("REVOKE", operatorUserId, row.getId(), "communityId=" + row.getCommunityId());
        return toResponse(row);
    }

    private CommunityNoticeResponse toResponse(CommunityNoticeEntity row) {
        return new CommunityNoticeResponse(
                row.getId(),
                row.getCommunityId(),
                row.getTitle(),
                row.getContent(),
                row.getStatus(),
                row.getCreatedBy(),
                row.getRevokedBy(),
                row.getRevokedAt(),
                row.getCreatedAt(),
                row.getUpdatedAt()
        );
    }

    private void appendLog(String action, Long operatorUserId, Long targetId, String details) {
        LocalDateTime now = LocalDateTime.now();
        AdminOperationLogEntity log = new AdminOperationLogEntity();
        log.setModule("COMMUNITY_NOTICE");
        log.setAction(action);
        log.setOperatorUserId(operatorUserId);
        log.setTargetType("NOTICE");
        log.setTargetId(targetId);
        log.setDetails(details);
        log.setStatus(1);
        log.setCreatedAt(now);
        log.setUpdatedAt(now);
        adminOperationLogMapper.insert(log);
    }
}
