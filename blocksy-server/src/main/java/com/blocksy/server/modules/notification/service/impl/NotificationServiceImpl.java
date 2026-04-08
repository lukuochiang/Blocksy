package com.blocksy.server.modules.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blocksy.server.modules.notification.dto.NotificationResponse;
import com.blocksy.server.modules.notification.entity.NotificationEntity;
import com.blocksy.server.modules.notification.mapper.NotificationMapper;
import com.blocksy.server.modules.notification.service.NotificationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;

    public NotificationServiceImpl(NotificationMapper notificationMapper) {
        this.notificationMapper = notificationMapper;
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
}
