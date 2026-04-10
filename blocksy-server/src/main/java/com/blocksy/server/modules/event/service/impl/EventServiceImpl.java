package com.blocksy.server.modules.event.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blocksy.server.common.exception.BusinessException;
import com.blocksy.server.modules.event.dto.AdminEventHandleLogResponse;
import com.blocksy.server.modules.event.dto.AdminEventBatchHandleRequest;
import com.blocksy.server.modules.event.dto.AdminEventBatchHandleResponse;
import com.blocksy.server.modules.event.dto.AdminEventBatchItemResult;
import com.blocksy.server.modules.event.dto.AdminEventBatchRetryRequest;
import com.blocksy.server.modules.event.dto.AdminEventResponse;
import com.blocksy.server.modules.event.dto.EventRequest;
import com.blocksy.server.modules.event.dto.EventResponse;
import com.blocksy.server.modules.event.dto.EventSignupRequest;
import com.blocksy.server.modules.event.dto.EventSignupResponse;
import com.blocksy.server.modules.event.entity.EventEntity;
import com.blocksy.server.modules.event.entity.EventHandleLogEntity;
import com.blocksy.server.modules.event.entity.EventSignupEntity;
import com.blocksy.server.modules.event.mapper.EventHandleLogMapper;
import com.blocksy.server.modules.event.mapper.EventMapper;
import com.blocksy.server.modules.event.mapper.EventSignupMapper;
import com.blocksy.server.modules.event.service.EventService;
import com.blocksy.server.modules.notification.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class EventServiceImpl implements EventService {

    private final EventMapper eventMapper;
    private final EventSignupMapper eventSignupMapper;
    private final EventHandleLogMapper eventHandleLogMapper;
    private final NotificationService notificationService;

    public EventServiceImpl(
            EventMapper eventMapper,
            EventSignupMapper eventSignupMapper,
            EventHandleLogMapper eventHandleLogMapper,
            NotificationService notificationService
    ) {
        this.eventMapper = eventMapper;
        this.eventSignupMapper = eventSignupMapper;
        this.eventHandleLogMapper = eventHandleLogMapper;
        this.notificationService = notificationService;
    }

    @Override
    public List<EventResponse> list(Long communityId) {
        LambdaQueryWrapper<EventEntity> query = new LambdaQueryWrapper<EventEntity>()
                .eq(EventEntity::getStatus, 1)
                .orderByDesc(EventEntity::getStartTime)
                .last("LIMIT 50");
        if (communityId != null) {
            query.eq(EventEntity::getCommunityId, communityId);
        }
        return eventMapper.selectList(query).stream().map(this::toResponse).toList();
    }

    @Override
    public EventResponse getById(Long id) {
        EventEntity entity = eventMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("活动不存在");
        }
        return toResponse(entity);
    }

    @Override
    public List<EventResponse> listMine(Long userId, Long communityId) {
        LambdaQueryWrapper<EventEntity> query = new LambdaQueryWrapper<EventEntity>()
                .eq(EventEntity::getUserId, userId)
                .eq(EventEntity::getStatus, 1)
                .orderByDesc(EventEntity::getCreatedAt)
                .last("LIMIT 50");
        if (communityId != null) {
            query.eq(EventEntity::getCommunityId, communityId);
        }
        return eventMapper.selectList(query).stream().map(this::toResponse).toList();
    }

    @Override
    public List<EventResponse> listMySignups(Long userId) {
        List<EventSignupEntity> signups = eventSignupMapper.selectList(
                new LambdaQueryWrapper<EventSignupEntity>()
                        .eq(EventSignupEntity::getUserId, userId)
                        .eq(EventSignupEntity::getStatus, 1)
                        .orderByDesc(EventSignupEntity::getCreatedAt)
                        .last("LIMIT 50")
        );
        if (signups.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> eventIds = signups.stream().map(EventSignupEntity::getEventId).toList();
        return eventMapper.selectList(
                new LambdaQueryWrapper<EventEntity>()
                        .in(EventEntity::getId, eventIds)
                        .orderByDesc(EventEntity::getStartTime)
        ).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EventResponse create(Long userId, EventRequest request) {
        if (request.endTime() != null && request.endTime().isBefore(request.startTime())) {
            throw new BusinessException("结束时间不能早于开始时间");
        }
        LocalDateTime now = LocalDateTime.now();
        EventEntity entity = new EventEntity();
        entity.setUserId(userId);
        entity.setCommunityId(request.communityId());
        entity.setTitle(request.title());
        entity.setContent(request.content());
        entity.setLocation(request.location());
        entity.setCoverObjectKey(request.coverObjectKey());
        entity.setCoverUrl(request.coverUrl());
        entity.setStartTime(request.startTime());
        entity.setEndTime(request.endTime());
        entity.setSignupLimit(request.signupLimit());
        entity.setSignupCount(0);
        entity.setStatus(1);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        eventMapper.insert(entity);
        return toResponse(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EventSignupResponse signup(Long userId, Long eventId, EventSignupRequest request) {
        EventEntity event = eventMapper.selectById(eventId);
        if (event == null) {
            throw new IllegalArgumentException("活动不存在");
        }
        if (event.getStatus() == null || event.getStatus() != 1) {
            throw new BusinessException("活动已下架，暂不可报名");
        }

        EventSignupEntity existing = eventSignupMapper.selectOne(
                new LambdaQueryWrapper<EventSignupEntity>()
                        .eq(EventSignupEntity::getEventId, eventId)
                        .eq(EventSignupEntity::getUserId, userId)
                        .eq(EventSignupEntity::getStatus, 1)
                        .last("LIMIT 1")
        );
        if (existing != null) {
            throw new IllegalArgumentException("您已报名该活动");
        }

        if (event.getSignupLimit() != null && event.getSignupCount() != null && event.getSignupCount() >= event.getSignupLimit()) {
            throw new IllegalArgumentException("活动报名已满");
        }
        if (event.getEndTime() != null && event.getEndTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("活动已结束，不能报名");
        }

        LocalDateTime now = LocalDateTime.now();
        EventSignupEntity signup = new EventSignupEntity();
        signup.setEventId(eventId);
        signup.setUserId(userId);
        signup.setRemark(request == null ? null : request.remark());
        signup.setStatus(1);
        signup.setCreatedAt(now);
        signup.setUpdatedAt(now);
        eventSignupMapper.insert(signup);

        event.setSignupCount((event.getSignupCount() == null ? 0 : event.getSignupCount()) + 1);
        event.setUpdatedAt(now);
        eventMapper.updateById(event);

        if (!userId.equals(event.getUserId())) {
            notificationService.create(
                    event.getUserId(),
                    "EVENT_SIGNUP",
                    "活动报名提醒",
                    "你的活动「" + event.getTitle() + "」收到一条新报名",
                    event.getId(),
                    "EVENT"
            );
        }

        return new EventSignupResponse(signup.getId(), signup.getEventId(), signup.getUserId(), signup.getRemark(), signup.getCreatedAt());
    }

    @Override
    public List<AdminEventResponse> listForAdmin(Integer status, Long communityId, String keyword) {
        LambdaQueryWrapper<EventEntity> query = new LambdaQueryWrapper<EventEntity>()
                .orderByDesc(EventEntity::getCreatedAt)
                .last("LIMIT 200");
        if (status != null) {
            query.eq(EventEntity::getStatus, status);
        }
        if (communityId != null) {
            query.eq(EventEntity::getCommunityId, communityId);
        }
        if (keyword != null && !keyword.isBlank()) {
            query.and(q -> q.like(EventEntity::getTitle, keyword.trim()).or().like(EventEntity::getContent, keyword.trim()));
        }
        return eventMapper.selectList(query).stream().map(this::toAdminResponse).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminEventResponse handleForAdmin(Long eventId, Long operatorUserId, String action, String note) {
        EventEntity event = eventMapper.selectById(eventId);
        if (event == null) {
            throw new BusinessException("活动不存在");
        }
        String normalizedAction = action == null ? "" : action.trim().toUpperCase();
        int targetStatus;
        if ("OFFLINE".equals(normalizedAction) || "DELETE".equals(normalizedAction)) {
            targetStatus = 0;
        } else if ("RESTORE".equals(normalizedAction)) {
            targetStatus = 1;
        } else {
            throw new BusinessException("不支持的动作，仅支持 OFFLINE/RESTORE/DELETE");
        }

        event.setStatus(targetStatus);
        event.setUpdatedAt(LocalDateTime.now());
        eventMapper.updateById(event);

        LocalDateTime now = LocalDateTime.now();
        EventHandleLogEntity log = new EventHandleLogEntity();
        log.setEventId(eventId);
        log.setOperatorUserId(operatorUserId);
        log.setAction(normalizedAction);
        log.setNote(note);
        log.setStatus(1);
        log.setCreatedAt(now);
        log.setUpdatedAt(now);
        eventHandleLogMapper.insert(log);
        if (!operatorUserId.equals(event.getUserId())) {
            String content = switch (normalizedAction) {
                case "OFFLINE" -> "你的活动「" + event.getTitle() + "」已被下架。";
                case "RESTORE" -> "你的活动「" + event.getTitle() + "」已恢复展示。";
                case "DELETE" -> "你的活动「" + event.getTitle() + "」已删除。";
                default -> "你的活动「" + event.getTitle() + "」状态已更新。";
            };
            notificationService.create(
                    event.getUserId(),
                    "SYSTEM",
                    "活动状态变更通知",
                    content,
                    event.getId(),
                    "EVENT"
            );
        }
        return toAdminResponse(event);
    }

    @Override
    public List<AdminEventHandleLogResponse> listHandleLogsForAdmin(Long eventId) {
        return listHandleLogsForAdmin(eventId, null, null, null, null);
    }

    @Override
    public List<AdminEventHandleLogResponse> listHandleLogsForAdmin(
            Long eventId,
            Long operatorUserId,
            String action,
            LocalDateTime startAt,
            LocalDateTime endAt
    ) {
        LambdaQueryWrapper<EventHandleLogEntity> query = new LambdaQueryWrapper<EventHandleLogEntity>()
                .eq(EventHandleLogEntity::getStatus, 1)
                .orderByDesc(EventHandleLogEntity::getCreatedAt)
                .last("LIMIT 500");
        if (eventId != null) {
            query.eq(EventHandleLogEntity::getEventId, eventId);
        }
        if (operatorUserId != null) {
            query.eq(EventHandleLogEntity::getOperatorUserId, operatorUserId);
        }
        if (StringUtils.hasText(action)) {
            query.eq(EventHandleLogEntity::getAction, action.trim().toUpperCase());
        }
        if (startAt != null) {
            query.ge(EventHandleLogEntity::getCreatedAt, startAt);
        }
        if (endAt != null) {
            query.le(EventHandleLogEntity::getCreatedAt, endAt);
        }
        return eventHandleLogMapper.selectList(query).stream().map(log -> new AdminEventHandleLogResponse(
                log.getId(),
                log.getEventId(),
                log.getOperatorUserId(),
                log.getAction(),
                log.getNote(),
                log.getCreatedAt()
        )).toList();
    }

    @Override
    public String exportHandleLogsCsv(
            Long eventId,
            Long operatorUserId,
            String action,
            LocalDateTime startAt,
            LocalDateTime endAt
    ) {
        List<AdminEventHandleLogResponse> rows = listHandleLogsForAdmin(eventId, operatorUserId, action, startAt, endAt);
        StringBuilder csv = new StringBuilder("log_id,event_id,operator_user_id,action,note,created_at\n");
        for (AdminEventHandleLogResponse row : rows) {
            csv.append(row.id()).append(',')
                    .append(row.eventId()).append(',')
                    .append(row.operatorUserId()).append(',')
                    .append(escapeCsv(row.action())).append(',')
                    .append(escapeCsv(row.note())).append(',')
                    .append(row.createdAt() == null ? "" : row.createdAt())
                    .append('\n');
        }
        return csv.toString();
    }

    @Override
    public AdminEventBatchHandleResponse batchHandleForAdmin(Long operatorUserId, AdminEventBatchHandleRequest request) {
        List<Long> successIds = new java.util.ArrayList<>();
        List<Long> skippedIds = new java.util.ArrayList<>();
        List<AdminEventBatchItemResult> failedItems = new java.util.ArrayList<>();

        for (Long eventId : request.eventIds()) {
            if (eventId == null) {
                failedItems.add(new AdminEventBatchItemResult(null, "eventId 不能为空"));
                continue;
            }
            EventEntity entity = eventMapper.selectById(eventId);
            if (entity == null) {
                failedItems.add(new AdminEventBatchItemResult(eventId, "活动不存在"));
                continue;
            }

            String action = request.action() == null ? "" : request.action().trim().toUpperCase();
            boolean needSkip = ("RESTORE".equals(action) && entity.getStatus() != null && entity.getStatus() == 1)
                    || (("OFFLINE".equals(action) || "DELETE".equals(action)) && entity.getStatus() != null && entity.getStatus() == 0);
            if (needSkip) {
                skippedIds.add(eventId);
                continue;
            }
            try {
                handleForAdmin(eventId, operatorUserId, action, request.note());
                successIds.add(eventId);
            } catch (Exception ex) {
                failedItems.add(new AdminEventBatchItemResult(eventId, ex.getMessage()));
            }
        }
        return new AdminEventBatchHandleResponse(
                request.eventIds().size(),
                successIds.size(),
                successIds,
                skippedIds,
                failedItems
        );
    }

    @Override
    public AdminEventBatchHandleResponse batchRetryForAdmin(Long operatorUserId, AdminEventBatchRetryRequest request) {
        return batchHandleForAdmin(
                operatorUserId,
                new AdminEventBatchHandleRequest(request.failedEventIds(), request.action(), request.note())
        );
    }

    @Override
    public String exportBatchHandleResultCsv(AdminEventBatchHandleResponse response) {
        StringBuilder csv = new StringBuilder("status,event_id,message\n");
        if (response.successIds() != null) {
            for (Long eventId : response.successIds()) {
                csv.append("SUCCESS,").append(eventId == null ? "" : eventId).append(",ok\n");
            }
        }
        if (response.skippedIds() != null) {
            for (Long eventId : response.skippedIds()) {
                csv.append("SKIPPED,").append(eventId == null ? "" : eventId).append(",already_in_target_status\n");
            }
        }
        if (response.failedItems() != null) {
            for (AdminEventBatchItemResult failed : response.failedItems()) {
                csv.append("FAILED,")
                        .append(failed.eventId() == null ? "" : failed.eventId())
                        .append(',')
                        .append(escapeCsv(failed.message()))
                        .append('\n');
            }
        }
        return csv.toString();
    }

    private EventResponse toResponse(EventEntity entity) {
        return new EventResponse(
                entity.getId(),
                entity.getCommunityId(),
                entity.getUserId(),
                entity.getTitle(),
                entity.getContent(),
                entity.getLocation(),
                entity.getCoverObjectKey(),
                entity.getCoverUrl(),
                entity.getStartTime(),
                entity.getEndTime(),
                entity.getSignupLimit(),
                entity.getSignupCount(),
                entity.getStatus()
        );
    }

    private AdminEventResponse toAdminResponse(EventEntity entity) {
        return new AdminEventResponse(
                entity.getId(),
                entity.getUserId(),
                entity.getCommunityId(),
                entity.getTitle(),
                entity.getContent(),
                entity.getStartTime(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
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
