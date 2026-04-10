package com.blocksy.server.modules.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blocksy.server.common.api.PageResponse;
import com.blocksy.server.modules.admin.dto.AdminEventSignupResponse;
import com.blocksy.server.modules.admin.service.AdminEventSignupService;
import com.blocksy.server.modules.event.entity.EventEntity;
import com.blocksy.server.modules.event.entity.EventSignupEntity;
import com.blocksy.server.modules.event.mapper.EventMapper;
import com.blocksy.server.modules.event.mapper.EventSignupMapper;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminEventSignupServiceImpl implements AdminEventSignupService {

    private final EventSignupMapper eventSignupMapper;
    private final EventMapper eventMapper;

    public AdminEventSignupServiceImpl(EventSignupMapper eventSignupMapper, EventMapper eventMapper) {
        this.eventSignupMapper = eventSignupMapper;
        this.eventMapper = eventMapper;
    }

    @Override
    public PageResponse<AdminEventSignupResponse> page(Long eventId, Long communityId, Long userId, Integer page, Integer pageSize) {
        int currentPage = page == null || page < 1 ? 1 : page;
        int size = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 100);
        LambdaQueryWrapper<EventSignupEntity> countQuery = new LambdaQueryWrapper<EventSignupEntity>()
                .eq(EventSignupEntity::getStatus, 1);
        if (eventId != null) {
            countQuery.eq(EventSignupEntity::getEventId, eventId);
        }
        if (userId != null) {
            countQuery.eq(EventSignupEntity::getUserId, userId);
        }
        long total = eventSignupMapper.selectCount(countQuery);
        if (total == 0) {
            return new PageResponse<>(currentPage, size, 0L, Collections.emptyList());
        }

        LambdaQueryWrapper<EventSignupEntity> pageQuery = new LambdaQueryWrapper<EventSignupEntity>()
                .eq(EventSignupEntity::getStatus, 1)
                .orderByDesc(EventSignupEntity::getCreatedAt)
                .last("LIMIT " + size + " OFFSET " + ((currentPage - 1) * size));
        if (eventId != null) {
            pageQuery.eq(EventSignupEntity::getEventId, eventId);
        }
        if (userId != null) {
            pageQuery.eq(EventSignupEntity::getUserId, userId);
        }
        List<EventSignupEntity> signups = eventSignupMapper.selectList(pageQuery);
        List<Long> eventIds = signups.stream().map(EventSignupEntity::getEventId).distinct().toList();
        Map<Long, EventEntity> eventMap = new HashMap<>();
        if (!eventIds.isEmpty()) {
            for (EventEntity event : eventMapper.selectList(new LambdaQueryWrapper<EventEntity>().in(EventEntity::getId, eventIds))) {
                if (communityId != null && (event.getCommunityId() == null || !communityId.equals(event.getCommunityId()))) {
                    continue;
                }
                eventMap.put(event.getId(), event);
            }
        }
        List<AdminEventSignupResponse> items = signups.stream()
                .filter(signup -> eventMap.containsKey(signup.getEventId()))
                .map(signup -> {
                    EventEntity event = eventMap.get(signup.getEventId());
                    return new AdminEventSignupResponse(
                            signup.getId(),
                            signup.getEventId(),
                            event == null ? null : event.getTitle(),
                            event == null ? null : event.getCommunityId(),
                            signup.getUserId(),
                            signup.getRemark(),
                            signup.getStatus(),
                            signup.getCreatedAt()
                    );
                })
                .toList();
        return new PageResponse<>(currentPage, size, total, items);
    }
}
