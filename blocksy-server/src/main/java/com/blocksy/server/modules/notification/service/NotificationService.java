package com.blocksy.server.modules.notification.service;

import com.blocksy.server.common.api.PageResponse;
import com.blocksy.server.modules.notification.dto.AdminAnnouncementItemResponse;
import com.blocksy.server.modules.notification.dto.NotificationResponse;
import com.blocksy.server.modules.notification.dto.NotificationPageResponse;
import com.blocksy.server.modules.notification.dto.NotificationStatsResponse;
import com.blocksy.server.modules.notification.dto.NotificationTrendPointResponse;

import java.util.List;

public interface NotificationService {
    List<NotificationResponse> listByUserId(Long userId);

    NotificationPageResponse pageByUserId(Long userId, Integer page, Integer pageSize, String type, Boolean isRead);

    void create(Long userId, String type, String title, String content, Long relatedId, String relatedType);

    long countUnread(Long userId);

    void markRead(Long userId, Long notificationId);

    int markReadBatch(Long userId, List<Long> notificationIds);

    int markAllRead(Long userId);

    int createSystemAnnouncement(Long operatorUserId, String title, String content);

    NotificationStatsResponse stats();

    PageResponse<AdminAnnouncementItemResponse> pageAnnouncements(String keyword, Integer status, Integer page, Integer pageSize);

    void revokeAnnouncement(Long announcementId, Long operatorUserId);

    int redispatchAnnouncement(Long announcementId, Long operatorUserId);

    List<NotificationTrendPointResponse> trend(Integer days);
}
