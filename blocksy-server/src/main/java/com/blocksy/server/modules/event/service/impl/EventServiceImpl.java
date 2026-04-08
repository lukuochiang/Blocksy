package com.blocksy.server.modules.event.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blocksy.server.modules.event.dto.EventRequest;
import com.blocksy.server.modules.event.dto.EventResponse;
import com.blocksy.server.modules.event.dto.EventSignupRequest;
import com.blocksy.server.modules.event.dto.EventSignupResponse;
import com.blocksy.server.modules.event.entity.EventEntity;
import com.blocksy.server.modules.event.entity.EventSignupEntity;
import com.blocksy.server.modules.event.mapper.EventMapper;
import com.blocksy.server.modules.event.mapper.EventSignupMapper;
import com.blocksy.server.modules.event.service.EventService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EventServiceImpl implements EventService {

    private final EventMapper eventMapper;
    private final EventSignupMapper eventSignupMapper;

    public EventServiceImpl(EventMapper eventMapper, EventSignupMapper eventSignupMapper) {
        this.eventMapper = eventMapper;
        this.eventSignupMapper = eventSignupMapper;
    }

    @Override
    public List<EventResponse> list() {
        return eventMapper.selectList(
                new LambdaQueryWrapper<EventEntity>()
                        .eq(EventEntity::getStatus, 1)
                        .orderByDesc(EventEntity::getStartTime)
                        .last("LIMIT 30")
        ).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EventResponse create(Long userId, EventRequest request) {
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
        EventEntity event = eventMapper.selectOne(
                new LambdaQueryWrapper<EventEntity>()
                        .eq(EventEntity::getId, eventId)
                        .eq(EventEntity::getStatus, 1)
                        .last("LIMIT 1")
        );
        if (event == null) {
            throw new IllegalArgumentException("活动不存在");
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

        return new EventSignupResponse(signup.getId(), signup.getEventId(), signup.getUserId(), signup.getRemark(), signup.getCreatedAt());
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
                entity.getSignupCount()
        );
    }
}
