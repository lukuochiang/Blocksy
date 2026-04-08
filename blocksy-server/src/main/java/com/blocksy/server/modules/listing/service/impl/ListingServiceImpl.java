package com.blocksy.server.modules.listing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blocksy.server.modules.listing.dto.ListingRequest;
import com.blocksy.server.modules.listing.dto.ListingResponse;
import com.blocksy.server.modules.listing.entity.ListingEntity;
import com.blocksy.server.modules.listing.mapper.ListingMapper;
import com.blocksy.server.modules.listing.service.ListingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ListingServiceImpl implements ListingService {

    private final ListingMapper listingMapper;

    public ListingServiceImpl(ListingMapper listingMapper) {
        this.listingMapper = listingMapper;
    }

    @Override
    public List<ListingResponse> list() {
        return listingMapper.selectList(
                new LambdaQueryWrapper<ListingEntity>()
                        .eq(ListingEntity::getStatus, 1)
                        .orderByDesc(ListingEntity::getCreatedAt)
                        .last("LIMIT 30")
        ).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ListingResponse create(Long userId, ListingRequest request) {
        LocalDateTime now = LocalDateTime.now();
        ListingEntity entity = new ListingEntity();
        entity.setUserId(userId);
        entity.setCommunityId(request.communityId());
        entity.setCategory(request.category());
        entity.setTitle(request.title());
        entity.setContent(request.content());
        entity.setPrice(request.price() == null ? null : BigDecimal.valueOf(request.price()));
        entity.setContact(request.contact());
        entity.setCoverObjectKey(request.coverObjectKey());
        entity.setCoverUrl(request.coverUrl());
        entity.setStatus(1);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        listingMapper.insert(entity);
        return toResponse(entity);
    }

    private ListingResponse toResponse(ListingEntity entity) {
        return new ListingResponse(
                entity.getId(),
                entity.getCommunityId(),
                entity.getUserId(),
                entity.getCategory(),
                entity.getTitle(),
                entity.getContent(),
                entity.getPrice() == null ? null : entity.getPrice().doubleValue(),
                entity.getContact(),
                entity.getCoverObjectKey(),
                entity.getCoverUrl()
        );
    }
}
