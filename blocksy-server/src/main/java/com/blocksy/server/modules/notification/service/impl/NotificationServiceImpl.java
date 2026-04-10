package com.blocksy.server.modules.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.blocksy.server.common.api.PageResponse;
import com.blocksy.server.common.exception.BusinessException;
import com.blocksy.server.modules.notification.dto.AdminAnnouncementItemResponse;
import com.blocksy.server.modules.notification.dto.NotificationPageResponse;
import com.blocksy.server.modules.notification.dto.NotificationResponse;
import com.blocksy.server.modules.notification.dto.NotificationStatsResponse;
import com.blocksy.server.modules.notification.dto.NotificationTrendPointResponse;
import com.blocksy.server.modules.notification.dto.NotificationTypeStatItem;
import com.blocksy.server.modules.notification.entity.NotificationEntity;
import com.blocksy.server.modules.notification.entity.SystemAnnouncementEntity;
import com.blocksy.server.modules.notification.mapper.NotificationMapper;
import com.blocksy.server.modules.notification.mapper.SystemAnnouncementMapper;
import com.blocksy.server.modules.notification.service.NotificationService;
import com.blocksy.server.modules.user.entity.UserEntity;
import com.blocksy.server.modules.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;
    private final SystemAnnouncementMapper systemAnnouncementMapper;

    public NotificationServiceImpl(
            NotificationMapper notificationMapper,
            UserMapper userMapper,
            SystemAnnouncementMapper systemAnnouncementMapper
    ) {
        this.notificationMapper = notificationMapper;
        this.userMapper = userMapper;
        this.systemAnnouncementMapper = systemAnnouncementMapper;
    }

    @Override
    public List<NotificationResponse> listByUserId(Long userId) {
        return notificationMapper.selectList(
                new LambdaQueryWrapper<NotificationEntity>()
                        .eq(NotificationEntity::getUserId, userId)
                        .eq(NotificationEntity::getStatus, 1)
                        .orderByDesc(NotificationEntity::getCreatedAt)
                        .last("LIMIT 50")
        ).stream().map(n -> new NotificationResponse(
                n.getId(),
                n.getType(),
                n.getTitle(),
                n.getContent(),
                n.getRead(),
                n.getCreatedAt()
        )).toList();
    }

    @Override
    public NotificationPageResponse pageByUserId(Long userId, Integer page, Integer pageSize, String type, Boolean isRead) {
        int currentPage = page == null || page < 1 ? 1 : page;
        int size = pageSize == null || pageSize < 1 ? 20 : Math.min(pageSize, 100);
        LambdaQueryWrapper<NotificationEntity> query = new LambdaQueryWrapper<NotificationEntity>()
                .eq(NotificationEntity::getUserId, userId)
                .eq(NotificationEntity::getStatus, 1);
        if (type != null && !type.isBlank()) {
            String normalizedType = type.trim().toUpperCase();
            if ("INTERACTION".equals(normalizedType)) {
                query.in(NotificationEntity::getType, List.of("COMMENT", "LIKE"));
            } else if ("ACTIVITY".equals(normalizedType)) {
                query.in(NotificationEntity::getType, List.of("EVENT_SIGNUP"));
            } else if ("SYSTEM".equals(normalizedType)) {
                query.eq(NotificationEntity::getType, "SYSTEM");
            } else {
                query.eq(NotificationEntity::getType, normalizedType);
            }
        }
        if (isRead != null) {
            query.eq(NotificationEntity::getRead, isRead);
        }
        Long total = notificationMapper.selectCount(query);
        List<NotificationEntity> rows = notificationMapper.selectList(
                query.orderByDesc(NotificationEntity::getCreatedAt)
                        .last("LIMIT " + size + " OFFSET " + ((currentPage - 1) * size))
        );
        List<NotificationResponse> items = rows.stream().map(n -> new NotificationResponse(
                n.getId(),
                n.getType(),
                n.getTitle(),
                n.getContent(),
                n.getRead(),
                n.getCreatedAt()
        )).toList();
        return new NotificationPageResponse(currentPage, size, total, items);
    }

    @Override
    public void create(Long userId, String type, String title, String content, Long relatedId, String relatedType) {
        LocalDateTime now = LocalDateTime.now();
        NotificationEntity entity = new NotificationEntity();
        entity.setUserId(userId);
        entity.setType(type);
        entity.setTitle(title);
        entity.setContent(content);
        entity.setRelatedId(relatedId);
        entity.setRelatedType(relatedType);
        entity.setRead(false);
        entity.setStatus(1);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        notificationMapper.insert(entity);
    }

    @Override
    public long countUnread(Long userId) {
        return notificationMapper.selectCount(
                new LambdaQueryWrapper<NotificationEntity>()
                        .eq(NotificationEntity::getUserId, userId)
                        .eq(NotificationEntity::getStatus, 1)
                        .eq(NotificationEntity::getRead, false)
        );
    }

    @Override
    public void markRead(Long userId, Long notificationId) {
        NotificationEntity entity = notificationMapper.selectOne(
                new LambdaQueryWrapper<NotificationEntity>()
                        .eq(NotificationEntity::getId, notificationId)
                        .eq(NotificationEntity::getUserId, userId)
                        .eq(NotificationEntity::getStatus, 1)
                        .last("LIMIT 1")
        );
        if (entity == null) {
            throw new BusinessException("通知不存在");
        }
        if (Boolean.TRUE.equals(entity.getRead())) {
            return;
        }
        notificationMapper.update(
                null,
                new LambdaUpdateWrapper<NotificationEntity>()
                        .eq(NotificationEntity::getId, notificationId)
                        .set(NotificationEntity::getRead, true)
                        .set(NotificationEntity::getUpdatedAt, LocalDateTime.now())
        );
    }

    @Override
    public int markReadBatch(Long userId, List<Long> notificationIds) {
        if (notificationIds == null || notificationIds.isEmpty()) {
            throw new BusinessException("notificationIds 不能为空");
        }
        return notificationMapper.update(
                null,
                new LambdaUpdateWrapper<NotificationEntity>()
                        .eq(NotificationEntity::getUserId, userId)
                        .eq(NotificationEntity::getStatus, 1)
                        .in(NotificationEntity::getId, notificationIds)
                        .eq(NotificationEntity::getRead, false)
                        .set(NotificationEntity::getRead, true)
                        .set(NotificationEntity::getUpdatedAt, LocalDateTime.now())
        );
    }

    @Override
    public int markAllRead(Long userId) {
        return notificationMapper.update(
                null,
                new LambdaUpdateWrapper<NotificationEntity>()
                        .eq(NotificationEntity::getUserId, userId)
                        .eq(NotificationEntity::getStatus, 1)
                        .eq(NotificationEntity::getRead, false)
                        .set(NotificationEntity::getRead, true)
                        .set(NotificationEntity::getUpdatedAt, LocalDateTime.now())
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int createSystemAnnouncement(Long operatorUserId, String title, String content) {
        LocalDateTime now = LocalDateTime.now();
        SystemAnnouncementEntity announcement = new SystemAnnouncementEntity();
        announcement.setTitle(title);
        announcement.setContent(content);
        announcement.setStatus(1);
        announcement.setCreatedBy(operatorUserId);
        announcement.setDispatchCount(0);
        announcement.setCreatedAt(now);
        announcement.setUpdatedAt(now);
        systemAnnouncementMapper.insert(announcement);
        return dispatchAnnouncement(announcement, operatorUserId);
    }

    @Override
    public PageResponse<AdminAnnouncementItemResponse> pageAnnouncements(String keyword, Integer status, Integer page, Integer pageSize) {
        int currentPage = page == null || page < 1 ? 1 : page;
        int size = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 100);
        LambdaQueryWrapper<SystemAnnouncementEntity> countQuery = new LambdaQueryWrapper<>();
        if (status != null) {
            countQuery.eq(SystemAnnouncementEntity::getStatus, status);
        }
        if (keyword != null && !keyword.isBlank()) {
            String trimmed = keyword.trim();
            countQuery.and(wrapper -> wrapper.like(SystemAnnouncementEntity::getTitle, trimmed)
                    .or()
                    .like(SystemAnnouncementEntity::getContent, trimmed));
        }
        long total = systemAnnouncementMapper.selectCount(countQuery);
        if (total == 0) {
            return new PageResponse<>(currentPage, size, 0L, Collections.emptyList());
        }
        LambdaQueryWrapper<SystemAnnouncementEntity> pageQuery = new LambdaQueryWrapper<SystemAnnouncementEntity>()
                .orderByDesc(SystemAnnouncementEntity::getCreatedAt)
                .last("LIMIT " + size + " OFFSET " + ((currentPage - 1) * size));
        if (status != null) {
            pageQuery.eq(SystemAnnouncementEntity::getStatus, status);
        }
        if (keyword != null && !keyword.isBlank()) {
            String trimmed = keyword.trim();
            pageQuery.and(wrapper -> wrapper.like(SystemAnnouncementEntity::getTitle, trimmed)
                    .or()
                    .like(SystemAnnouncementEntity::getContent, trimmed));
        }
        List<AdminAnnouncementItemResponse> items = systemAnnouncementMapper.selectList(pageQuery)
                .stream()
                .map(row -> new AdminAnnouncementItemResponse(
                        row.getId(),
                        row.getTitle(),
                        row.getContent(),
                        row.getStatus(),
                        row.getDispatchCount(),
                        row.getLastDispatchedAt(),
                        row.getCreatedAt(),
                        row.getUpdatedAt()
                ))
                .toList();
        return new PageResponse<>(currentPage, size, total, items);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revokeAnnouncement(Long announcementId, Long operatorUserId) {
        SystemAnnouncementEntity announcement = systemAnnouncementMapper.selectById(announcementId);
        if (announcement == null) {
            throw new BusinessException("公告不存在");
        }
        if (announcement.getStatus() != null && announcement.getStatus() == 0) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        announcement.setStatus(0);
        announcement.setRevokedBy(operatorUserId);
        announcement.setRevokedAt(now);
        announcement.setUpdatedAt(now);
        systemAnnouncementMapper.updateById(announcement);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int redispatchAnnouncement(Long announcementId, Long operatorUserId) {
        SystemAnnouncementEntity announcement = systemAnnouncementMapper.selectById(announcementId);
        if (announcement == null) {
            throw new BusinessException("公告不存在");
        }
        if (announcement.getStatus() != null && announcement.getStatus() == 0) {
            throw new BusinessException("公告已撤回，不能二次下发");
        }
        return dispatchAnnouncement(announcement, operatorUserId);
    }

    @Override
    public List<NotificationTrendPointResponse> trend(Integer days) {
        int resolvedDays = (days == null || days <= 0) ? 7 : Math.min(days, 30);
        LocalDate startDate = LocalDate.now().minusDays(resolvedDays - 1L);
        LocalDateTime startAt = startDate.atStartOfDay();

        Map<LocalDate, long[]> trendMap = new LinkedHashMap<>();
        for (int i = 0; i < resolvedDays; i++) {
            trendMap.put(startDate.plusDays(i), new long[]{0L, 0L});
        }

        List<NotificationEntity> rows = notificationMapper.selectList(
                new LambdaQueryWrapper<NotificationEntity>()
                        .eq(NotificationEntity::getStatus, 1)
                        .eq(NotificationEntity::getType, "SYSTEM")
                        .eq(NotificationEntity::getRelatedType, "SYSTEM_ANNOUNCEMENT")
                        .ge(NotificationEntity::getCreatedAt, startAt)
                        .select(NotificationEntity::getCreatedAt, NotificationEntity::getRead)
                        .last("LIMIT 200000")
        );

        for (NotificationEntity row : rows) {
            if (row.getCreatedAt() == null) {
                continue;
            }
            LocalDate day = row.getCreatedAt().toLocalDate();
            long[] stats = trendMap.get(day);
            if (stats == null) {
                continue;
            }
            stats[0]++;
            if (Boolean.TRUE.equals(row.getRead())) {
                stats[1]++;
            }
        }

        List<NotificationTrendPointResponse> result = new ArrayList<>();
        for (Map.Entry<LocalDate, long[]> entry : trendMap.entrySet()) {
            long total = entry.getValue()[0];
            long read = entry.getValue()[1];
            double readRate = total == 0 ? 0D : (double) read / total;
            result.add(new NotificationTrendPointResponse(entry.getKey().toString(), total, read, readRate));
        }
        return result;
    }

    private int dispatchAnnouncement(SystemAnnouncementEntity announcement, Long operatorUserId) {
        List<UserEntity> users = userMapper.selectList(
                new LambdaQueryWrapper<UserEntity>()
                        .eq(UserEntity::getStatus, 1)
                        .select(UserEntity::getId)
                        .last("LIMIT 10000")
        );
        int count = 0;
        for (UserEntity user : users) {
            create(
                    user.getId(),
                    "SYSTEM",
                    announcement.getTitle(),
                    announcement.getContent(),
                    announcement.getId(),
                    "SYSTEM_ANNOUNCEMENT"
            );
            count++;
        }
        LocalDateTime now = LocalDateTime.now();
        announcement.setDispatchCount((announcement.getDispatchCount() == null ? 0 : announcement.getDispatchCount()) + count);
        announcement.setLastDispatchedAt(now);
        announcement.setUpdatedAt(now);
        if (announcement.getCreatedBy() == null) {
            announcement.setCreatedBy(operatorUserId);
        }
        systemAnnouncementMapper.updateById(announcement);
        return count;
    }

    @Override
    public NotificationStatsResponse stats() {
        List<NotificationEntity> rows = notificationMapper.selectList(
                new LambdaQueryWrapper<NotificationEntity>()
                        .eq(NotificationEntity::getStatus, 1)
                        .select(NotificationEntity::getType, NotificationEntity::getRead, NotificationEntity::getCreatedAt)
                        .last("LIMIT 100000")
        );
        long total = rows.size();
        long read = rows.stream().filter(row -> Boolean.TRUE.equals(row.getRead())).count();
        long unread = total - read;
        OffsetDateTime startOfDay = OffsetDateTime.now().toLocalDate().atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();
        long today = rows.stream().filter(row -> {
            LocalDateTime createdAt = row.getCreatedAt();
            return createdAt != null && createdAt.atZone(ZoneId.systemDefault()).toOffsetDateTime().isAfter(startOfDay);
        }).count();

        Map<String, long[]> byType = new LinkedHashMap<>();
        for (NotificationEntity row : rows) {
            String type = row.getType() == null ? "UNKNOWN" : row.getType().trim().toUpperCase();
            byType.putIfAbsent(type, new long[3]);
            long[] values = byType.get(type);
            values[0]++;
            if (Boolean.TRUE.equals(row.getRead())) {
                values[1]++;
            } else {
                values[2]++;
            }
        }
        List<NotificationTypeStatItem> typeStats = new ArrayList<>();
        for (Map.Entry<String, long[]> entry : byType.entrySet()) {
            typeStats.add(new NotificationTypeStatItem(
                    entry.getKey(),
                    entry.getValue()[0],
                    entry.getValue()[1],
                    entry.getValue()[2]
            ));
        }
        double readRate = total == 0 ? 0 : (double) read / total;
        return new NotificationStatsResponse(total, read, unread, today, readRate, typeStats);
    }
}
