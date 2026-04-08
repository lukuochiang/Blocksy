package com.blocksy.server.modules.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blocksy.server.common.exception.BusinessException;
import com.blocksy.server.modules.community.dto.AdminCommunityCreateRequest;
import com.blocksy.server.modules.community.dto.CommunityResponse;
import com.blocksy.server.modules.community.entity.CommunityEntity;
import com.blocksy.server.modules.community.mapper.CommunityMapper;
import com.blocksy.server.modules.community.service.CommunityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommunityServiceImpl implements CommunityService {

    private final CommunityMapper communityMapper;

    public CommunityServiceImpl(CommunityMapper communityMapper) {
        this.communityMapper = communityMapper;
    }

    @Override
    public List<CommunityResponse> list() {
        return communityMapper.selectList(
                new LambdaQueryWrapper<CommunityEntity>()
                        .eq(CommunityEntity::getStatus, 1)
                        .orderByAsc(CommunityEntity::getId)
                        .last("LIMIT 100")
        ).stream().map(entity -> new CommunityResponse(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getAddress(),
                entity.getDescription()
        )).toList();
    }

    @Override
    public List<CommunityEntity> listForAdmin() {
        return communityMapper.selectList(
                new LambdaQueryWrapper<CommunityEntity>()
                        .orderByDesc(CommunityEntity::getCreatedAt)
                        .last("LIMIT 200")
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommunityEntity createForAdmin(AdminCommunityCreateRequest request) {
        CommunityEntity exists = communityMapper.selectOne(
                new LambdaQueryWrapper<CommunityEntity>()
                        .eq(CommunityEntity::getCode, request.code())
                        .last("LIMIT 1")
        );
        if (exists != null) {
            throw new BusinessException("社区编码已存在");
        }
        LocalDateTime now = LocalDateTime.now();
        CommunityEntity entity = new CommunityEntity();
        entity.setCode(request.code());
        entity.setName(request.name());
        entity.setAddress(request.address());
        entity.setDescription(request.description());
        entity.setStatus(1);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        communityMapper.insert(entity);
        return entity;
    }
}
