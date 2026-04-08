package com.blocksy.server.modules.notification.service;

import com.blocksy.server.modules.notification.dto.NotificationResponse;

import java.util.List;

public interface NotificationService {
    List<NotificationResponse> listByUserId(Long userId);
}
