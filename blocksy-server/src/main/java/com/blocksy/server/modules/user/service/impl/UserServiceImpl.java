package com.blocksy.server.modules.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blocksy.server.common.exception.BusinessException;
import com.blocksy.server.modules.community.entity.CommunityEntity;
import com.blocksy.server.modules.community.mapper.CommunityMapper;
import com.blocksy.server.modules.user.dto.UserCommunityItemResponse;
import com.blocksy.server.modules.user.entity.UserCommunityEntity;
import com.blocksy.server.modules.user.entity.UserEntity;
import com.blocksy.server.modules.user.mapper.UserCommunityMapper;
import com.blocksy.server.modules.user.mapper.UserMapper;
import com.blocksy.server.modules.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserCommunityMapper userCommunityMapper;
    private final CommunityMapper communityMapper;

    public UserServiceImpl(UserMapper userMapper, UserCommunityMapper userCommunityMapper, CommunityMapper communityMapper) {
        this.userMapper = userMapper;
        this.userCommunityMapper = userCommunityMapper;
        this.communityMapper = communityMapper;
    }

    @Override
    public UserEntity findByUsername(String username) {
        return userMapper.selectOne(
                new LambdaQueryWrapper<UserEntity>()
                        .eq(UserEntity::getUsername, username)
                        .last("LIMIT 1")
        );
    }

    @Override
    public UserEntity findById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public List<UserEntity> listActiveUsers() {
        return userMapper.selectList(
                new LambdaQueryWrapper<UserEntity>()
                        .orderByDesc(UserEntity::getCreatedAt)
                        .last("LIMIT 200")
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void banUser(Long userId) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setStatus(0);
        user.setUpdatedAt(OffsetDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbanUser(Long userId) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setStatus(1);
        user.setUpdatedAt(OffsetDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    public boolean isUserActive(Long userId) {
        UserEntity user = userMapper.selectById(userId);
        return user != null && user.getStatus() != null && user.getStatus() == 1;
    }

    @Override
    public List<UserCommunityItemResponse> listUserCommunities(Long userId) {
        List<UserCommunityEntity> relations = userCommunityMapper.selectList(
                new LambdaQueryWrapper<UserCommunityEntity>()
                        .eq(UserCommunityEntity::getUserId, userId)
                        .eq(UserCommunityEntity::getStatus, 1)
                        .orderByDesc(UserCommunityEntity::getIsDefault)
                        .orderByAsc(UserCommunityEntity::getCommunityId)
        );
        if (relations.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> communityIds = relations.stream().map(UserCommunityEntity::getCommunityId).toList();
        Map<Long, CommunityEntity> communityMap = communityMapper.selectList(
                new LambdaQueryWrapper<CommunityEntity>()
                        .in(CommunityEntity::getId, communityIds)
                        .eq(CommunityEntity::getStatus, 1)
        ).stream().collect(Collectors.toMap(CommunityEntity::getId, item -> item));

        return relations.stream()
                .filter(item -> communityMap.containsKey(item.getCommunityId()))
                .map(item -> {
                    CommunityEntity community = communityMap.get(item.getCommunityId());
                    return new UserCommunityItemResponse(
                            community.getId(),
                            community.getCode(),
                            community.getName(),
                            Boolean.TRUE.equals(item.getIsDefault())
                    );
                }).toList();
    }

    @Override
    public Long getDefaultCommunityId(Long userId) {
        UserCommunityEntity relation = userCommunityMapper.selectOne(
                new LambdaQueryWrapper<UserCommunityEntity>()
                        .eq(UserCommunityEntity::getUserId, userId)
                        .eq(UserCommunityEntity::getStatus, 1)
                        .eq(UserCommunityEntity::getIsDefault, true)
                        .last("LIMIT 1")
        );
        if (relation != null) {
            return relation.getCommunityId();
        }
        relation = userCommunityMapper.selectOne(
                new LambdaQueryWrapper<UserCommunityEntity>()
                        .eq(UserCommunityEntity::getUserId, userId)
                        .eq(UserCommunityEntity::getStatus, 1)
                        .orderByAsc(UserCommunityEntity::getId)
                        .last("LIMIT 1")
        );
        return relation == null ? null : relation.getCommunityId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void selectDefaultCommunity(Long userId, Long communityId) {
        CommunityEntity community = communityMapper.selectOne(
                new LambdaQueryWrapper<CommunityEntity>()
                        .eq(CommunityEntity::getId, communityId)
                        .eq(CommunityEntity::getStatus, 1)
                        .last("LIMIT 1")
        );
        if (community == null) {
            throw new BusinessException("社区不存在");
        }
        LocalDateTime now = LocalDateTime.now();
        List<UserCommunityEntity> allRelations = userCommunityMapper.selectList(
                new LambdaQueryWrapper<UserCommunityEntity>()
                        .eq(UserCommunityEntity::getUserId, userId)
        );
        UserCommunityEntity target = null;
        for (UserCommunityEntity relation : allRelations) {
            relation.setIsDefault(relation.getCommunityId().equals(communityId));
            relation.setUpdatedAt(now);
            userCommunityMapper.updateById(relation);
            if (relation.getCommunityId().equals(communityId)) {
                target = relation;
            }
        }
        if (target == null) {
            UserCommunityEntity newRelation = new UserCommunityEntity();
            newRelation.setUserId(userId);
            newRelation.setCommunityId(communityId);
            newRelation.setIsDefault(true);
            newRelation.setStatus(1);
            newRelation.setCreatedAt(now);
            newRelation.setUpdatedAt(now);
            userCommunityMapper.insert(newRelation);
        }
    }
}
