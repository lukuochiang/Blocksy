package com.blocksy.server.modules.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blocksy.server.common.api.PageResponse;
import com.blocksy.server.common.exception.BusinessException;
import com.blocksy.server.modules.admin.dto.NotificationTemplateResponse;
import com.blocksy.server.modules.admin.dto.NotificationTemplateSaveRequest;
import com.blocksy.server.modules.admin.dto.PushRecordResponse;
import com.blocksy.server.modules.admin.dto.PushTaskCreateRequest;
import com.blocksy.server.modules.admin.dto.PushTaskResponse;
import com.blocksy.server.modules.admin.entity.AdminOperationLogEntity;
import com.blocksy.server.modules.admin.entity.NotificationTemplateEntity;
import com.blocksy.server.modules.admin.entity.PushRecordEntity;
import com.blocksy.server.modules.admin.entity.PushTaskEntity;
import com.blocksy.server.modules.admin.mapper.AdminOperationLogMapper;
import com.blocksy.server.modules.admin.mapper.NotificationTemplateMapper;
import com.blocksy.server.modules.admin.mapper.PushRecordMapper;
import com.blocksy.server.modules.admin.mapper.PushTaskMapper;
import com.blocksy.server.modules.admin.service.AdminMessagingService;
import com.blocksy.server.modules.notification.service.NotificationService;
import com.blocksy.server.modules.user.entity.UserEntity;
import com.blocksy.server.modules.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class AdminMessagingServiceImpl implements AdminMessagingService {

    private final PushTaskMapper pushTaskMapper;
    private final PushRecordMapper pushRecordMapper;
    private final NotificationTemplateMapper notificationTemplateMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;
    private final AdminOperationLogMapper adminOperationLogMapper;

    public AdminMessagingServiceImpl(
            PushTaskMapper pushTaskMapper,
            PushRecordMapper pushRecordMapper,
            NotificationTemplateMapper notificationTemplateMapper,
            UserMapper userMapper,
            NotificationService notificationService,
            AdminOperationLogMapper adminOperationLogMapper
    ) {
        this.pushTaskMapper = pushTaskMapper;
        this.pushRecordMapper = pushRecordMapper;
        this.notificationTemplateMapper = notificationTemplateMapper;
        this.userMapper = userMapper;
        this.notificationService = notificationService;
        this.adminOperationLogMapper = adminOperationLogMapper;
    }

    @Override
    public PageResponse<PushTaskResponse> pagePushTasks(String taskStatus, String keyword, Integer page, Integer pageSize) {
        int currentPage = resolvePage(page);
        int size = resolvePageSize(pageSize);
        LambdaQueryWrapper<PushTaskEntity> query = new LambdaQueryWrapper<PushTaskEntity>()
                .eq(PushTaskEntity::getStatus, 1);
        if (taskStatus != null && !taskStatus.isBlank()) {
            query.eq(PushTaskEntity::getTaskStatus, taskStatus.trim().toUpperCase());
        }
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            query.and(wrapper -> wrapper.like(PushTaskEntity::getTitle, kw).or().like(PushTaskEntity::getContent, kw));
        }
        long total = pushTaskMapper.selectCount(query);
        if (total == 0) {
            return new PageResponse<>(currentPage, size, 0L, Collections.emptyList());
        }
        List<PushTaskResponse> items = pushTaskMapper.selectList(
                        query.orderByDesc(PushTaskEntity::getCreatedAt)
                                .last("LIMIT " + size + " OFFSET " + ((currentPage - 1) * size))
                ).stream()
                .map(this::toPushTaskResponse)
                .toList();
        return new PageResponse<>(currentPage, size, total, items);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PushTaskResponse createPushTask(Long adminUserId, PushTaskCreateRequest request) {
        LocalDateTime now = LocalDateTime.now();
        PushTaskEntity entity = new PushTaskEntity();
        entity.setTitle(request.title().trim());
        entity.setContent(request.content().trim());
        entity.setTargetType(request.targetType() == null || request.targetType().isBlank() ? "ALL" : request.targetType().trim().toUpperCase());
        entity.setTaskStatus("PENDING");
        entity.setCreatedBy(adminUserId);
        entity.setStatus(1);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        pushTaskMapper.insert(entity);
        appendLog("MESSAGE", "CREATE_PUSH_TASK", adminUserId, "PUSH_TASK", entity.getId(), entity.getTitle());
        return toPushTaskResponse(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int sendPushTask(Long taskId, Long adminUserId) {
        PushTaskEntity task = pushTaskMapper.selectById(taskId);
        if (task == null || task.getStatus() == null || task.getStatus() != 1) {
            throw new BusinessException("推送任务不存在");
        }
        List<UserEntity> users = userMapper.selectList(
                new LambdaQueryWrapper<UserEntity>()
                        .eq(UserEntity::getStatus, 1)
                        .select(UserEntity::getId)
                        .last("LIMIT 10000")
        );
        LocalDateTime now = LocalDateTime.now();
        int sentCount = 0;
        for (UserEntity user : users) {
            notificationService.create(user.getId(), "SYSTEM", task.getTitle(), task.getContent(), task.getId(), "PUSH_TASK");
            PushRecordEntity record = new PushRecordEntity();
            record.setTaskId(task.getId());
            record.setUserId(user.getId());
            record.setChannel("IN_APP");
            record.setSendStatus("SENT");
            record.setReadStatus(false);
            record.setDeliveredAt(now);
            record.setStatus(1);
            record.setCreatedAt(now);
            record.setUpdatedAt(now);
            pushRecordMapper.insert(record);
            sentCount++;
        }
        task.setTaskStatus("SENT");
        task.setSentAt(now);
        task.setUpdatedAt(now);
        pushTaskMapper.updateById(task);
        appendLog("MESSAGE", "SEND_PUSH_TASK", adminUserId, "PUSH_TASK", taskId, "sentCount=" + sentCount);
        return sentCount;
    }

    @Override
    public PageResponse<PushRecordResponse> pagePushRecords(Long taskId, String sendStatus, Integer page, Integer pageSize) {
        int currentPage = resolvePage(page);
        int size = resolvePageSize(pageSize);
        LambdaQueryWrapper<PushRecordEntity> query = new LambdaQueryWrapper<PushRecordEntity>()
                .eq(PushRecordEntity::getStatus, 1);
        if (taskId != null) {
            query.eq(PushRecordEntity::getTaskId, taskId);
        }
        if (sendStatus != null && !sendStatus.isBlank()) {
            query.eq(PushRecordEntity::getSendStatus, sendStatus.trim().toUpperCase());
        }
        long total = pushRecordMapper.selectCount(query);
        if (total == 0) {
            return new PageResponse<>(currentPage, size, 0L, Collections.emptyList());
        }
        List<PushRecordResponse> items = pushRecordMapper.selectList(
                        query.orderByDesc(PushRecordEntity::getCreatedAt)
                                .last("LIMIT " + size + " OFFSET " + ((currentPage - 1) * size))
                ).stream()
                .map(record -> new PushRecordResponse(
                        record.getId(),
                        record.getTaskId(),
                        record.getUserId(),
                        record.getChannel(),
                        record.getSendStatus(),
                        record.getReadStatus(),
                        record.getDeliveredAt(),
                        record.getReadAt(),
                        record.getCreatedAt()
                ))
                .toList();
        return new PageResponse<>(currentPage, size, total, items);
    }

    @Override
    public PageResponse<NotificationTemplateResponse> pageNotificationTemplates(String module, Boolean enabled, Integer page, Integer pageSize) {
        int currentPage = resolvePage(page);
        int size = resolvePageSize(pageSize);
        LambdaQueryWrapper<NotificationTemplateEntity> query = new LambdaQueryWrapper<NotificationTemplateEntity>()
                .eq(NotificationTemplateEntity::getStatus, 1);
        if (module != null && !module.isBlank()) {
            query.eq(NotificationTemplateEntity::getModule, module.trim().toUpperCase());
        }
        if (enabled != null) {
            query.eq(NotificationTemplateEntity::getEnabled, enabled);
        }
        long total = notificationTemplateMapper.selectCount(query);
        if (total == 0) {
            return new PageResponse<>(currentPage, size, 0L, Collections.emptyList());
        }
        List<NotificationTemplateResponse> items = notificationTemplateMapper.selectList(
                        query.orderByAsc(NotificationTemplateEntity::getModule)
                                .orderByAsc(NotificationTemplateEntity::getTriggerCode)
                                .last("LIMIT " + size + " OFFSET " + ((currentPage - 1) * size))
                ).stream()
                .map(this::toTemplateResponse)
                .toList();
        return new PageResponse<>(currentPage, size, total, items);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NotificationTemplateResponse saveNotificationTemplate(Long adminUserId, NotificationTemplateSaveRequest request) {
        String module = request.module().trim().toUpperCase();
        String triggerCode = request.triggerCode().trim().toUpperCase();
        LocalDateTime now = LocalDateTime.now();
        NotificationTemplateEntity existing = notificationTemplateMapper.selectOne(
                new LambdaQueryWrapper<NotificationTemplateEntity>()
                        .eq(NotificationTemplateEntity::getModule, module)
                        .eq(NotificationTemplateEntity::getTriggerCode, triggerCode)
                        .last("LIMIT 1")
        );
        if (existing == null) {
            NotificationTemplateEntity entity = new NotificationTemplateEntity();
            entity.setModule(module);
            entity.setTriggerCode(triggerCode);
            entity.setTitleTemplate(request.titleTemplate().trim());
            entity.setContentTemplate(request.contentTemplate().trim());
            entity.setEnabled(request.enabled() == null || request.enabled());
            entity.setStatus(1);
            entity.setCreatedAt(now);
            entity.setUpdatedAt(now);
            notificationTemplateMapper.insert(entity);
            appendLog("MESSAGE", "CREATE_NOTIFICATION_TEMPLATE", adminUserId, "NOTIFICATION_TEMPLATE", entity.getId(), module + ":" + triggerCode);
            return toTemplateResponse(entity);
        }
        existing.setTitleTemplate(request.titleTemplate().trim());
        existing.setContentTemplate(request.contentTemplate().trim());
        existing.setEnabled(request.enabled() == null || request.enabled());
        existing.setStatus(1);
        existing.setUpdatedAt(now);
        notificationTemplateMapper.updateById(existing);
        appendLog("MESSAGE", "UPDATE_NOTIFICATION_TEMPLATE", adminUserId, "NOTIFICATION_TEMPLATE", existing.getId(), module + ":" + triggerCode);
        return toTemplateResponse(existing);
    }

    private int resolvePage(Integer page) {
        return page == null || page < 1 ? 1 : page;
    }

    private int resolvePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return 20;
        }
        return Math.min(pageSize, 100);
    }

    private PushTaskResponse toPushTaskResponse(PushTaskEntity entity) {
        return new PushTaskResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getContent(),
                entity.getTargetType(),
                entity.getTaskStatus(),
                entity.getScheduledAt(),
                entity.getSentAt(),
                entity.getCreatedBy(),
                entity.getCreatedAt()
        );
    }

    private NotificationTemplateResponse toTemplateResponse(NotificationTemplateEntity entity) {
        return new NotificationTemplateResponse(
                entity.getId(),
                entity.getModule(),
                entity.getTriggerCode(),
                entity.getTitleTemplate(),
                entity.getContentTemplate(),
                entity.getEnabled(),
                entity.getUpdatedAt()
        );
    }

    private void appendLog(String module, String action, Long operatorUserId, String targetType, Long targetId, String details) {
        LocalDateTime now = LocalDateTime.now();
        AdminOperationLogEntity log = new AdminOperationLogEntity();
        log.setModule(module);
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
