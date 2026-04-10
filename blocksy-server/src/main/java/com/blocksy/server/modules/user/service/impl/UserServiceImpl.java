package com.blocksy.server.modules.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blocksy.server.common.exception.BusinessException;
import com.blocksy.server.modules.community.entity.CommunityEntity;
import com.blocksy.server.modules.community.mapper.CommunityMapper;
import com.blocksy.server.modules.user.dto.AdminUserPunishLogResponse;
import com.blocksy.server.modules.user.dto.UserCommunityItemResponse;
import com.blocksy.server.modules.user.entity.UserCommunityEntity;
import com.blocksy.server.modules.user.entity.UserEntity;
import com.blocksy.server.modules.user.entity.UserPunishLogEntity;
import com.blocksy.server.modules.user.mapper.UserCommunityMapper;
import com.blocksy.server.modules.user.mapper.UserMapper;
import com.blocksy.server.modules.user.mapper.UserPunishLogMapper;
import com.blocksy.server.modules.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
    private final UserPunishLogMapper userPunishLogMapper;

    public UserServiceImpl(
            UserMapper userMapper,
            UserCommunityMapper userCommunityMapper,
            CommunityMapper communityMapper,
            UserPunishLogMapper userPunishLogMapper
    ) {
        this.userMapper = userMapper;
        this.userCommunityMapper = userCommunityMapper;
        this.communityMapper = communityMapper;
        this.userPunishLogMapper = userPunishLogMapper;
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
    public List<UserEntity> listBlacklistedUsers() {
        return userMapper.selectList(
                new LambdaQueryWrapper<UserEntity>()
                        .eq(UserEntity::getStatus, 0)
                        .orderByDesc(UserEntity::getUpdatedAt)
                        .orderByDesc(UserEntity::getId)
                        .last("LIMIT 500")
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void banUser(Long userId) {
        banUser(userId, null, "系统封禁", null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void banUser(Long userId, Long operatorUserId, String reason, Integer durationHours) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (durationHours != null && durationHours <= 0) {
            throw new BusinessException("封禁时长必须大于 0");
        }
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime bannedUntil = durationHours == null ? null : now.plusHours(durationHours);
        user.setStatus(0);
        user.setBanReason(StringUtils.hasText(reason) ? reason : "管理员封禁");
        user.setBannedBy(operatorUserId);
        user.setBannedAt(now);
        user.setBannedUntil(bannedUntil);
        user.setUpdatedAt(now);
        userMapper.updateById(user);
        insertPunishLog(userId, operatorUserId, "BAN", user.getBanReason(), durationHours, bannedUntil);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbanUser(Long userId) {
        unbanUser(userId, null, "系统解封");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbanUser(Long userId, Long operatorUserId, String reason) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        OffsetDateTime now = OffsetDateTime.now();
        user.setStatus(1);
        user.setBanReason(null);
        user.setBannedBy(null);
        user.setBannedAt(null);
        user.setBannedUntil(null);
        user.setUpdatedAt(now);
        userMapper.updateById(user);
        insertPunishLog(userId, operatorUserId, "UNBAN", reason, null, null);
    }

    @Override
    public boolean isUserActive(Long userId) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null || user.getStatus() == null) {
            return false;
        }
        if (user.getStatus() == 1) {
            return true;
        }
        OffsetDateTime bannedUntil = user.getBannedUntil();
        if (bannedUntil != null && bannedUntil.isBefore(OffsetDateTime.now())) {
            unbanUser(userId, null, "封禁时效到期自动解封");
            return true;
        }
        return false;
    }

    @Override
    public List<AdminUserPunishLogResponse> listPunishLogs(Long userId) {
        return userPunishLogMapper.selectList(
                new LambdaQueryWrapper<UserPunishLogEntity>()
                        .eq(UserPunishLogEntity::getUserId, userId)
                        .eq(UserPunishLogEntity::getStatus, 1)
                        .orderByDesc(UserPunishLogEntity::getCreatedAt)
                        .last("LIMIT 100")
        ).stream().map(log -> new AdminUserPunishLogResponse(
                log.getId(),
                log.getUserId(),
                log.getOperatorUserId(),
                log.getAction(),
                log.getReason(),
                log.getDurationHours(),
                log.getExpiresAt(),
                log.getCreatedAt()
        )).toList();
    }

    @Override
    public List<UserCommunityItemResponse> listUserCommunities(Long userId) {
        List<UserCommunityEntity> relations = userCommunityMapper.selectList(
                new LambdaQueryWrapper<UserCommunityEntity>()
                        .eq(UserCommunityEntity::getUserId, userId)
                        .eq(UserCommunityEntity::getStatus, 1)
                        .orderByDesc(UserCommunityEntity::getDefaultFlag)
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
                            Boolean.TRUE.equals(item.getDefaultFlag())
                    );
                }).toList();
    }

    @Override
    public Long getDefaultCommunityId(Long userId) {
        UserCommunityEntity relation = userCommunityMapper.selectOne(
                new LambdaQueryWrapper<UserCommunityEntity>()
                        .eq(UserCommunityEntity::getUserId, userId)
                        .eq(UserCommunityEntity::getStatus, 1)
                        .eq(UserCommunityEntity::getDefaultFlag, true)
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
            relation.setDefaultFlag(relation.getCommunityId().equals(communityId));
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
            newRelation.setDefaultFlag(true);
            newRelation.setStatus(1);
            newRelation.setCreatedAt(now);
            newRelation.setUpdatedAt(now);
            userCommunityMapper.insert(newRelation);
        }
    }

    private void insertPunishLog(
            Long userId,
            Long operatorUserId,
            String action,
            String reason,
            Integer durationHours,
            OffsetDateTime expiresAt
    ) {
        OffsetDateTime now = OffsetDateTime.now();
        UserPunishLogEntity log = new UserPunishLogEntity();
        log.setUserId(userId);
        log.setOperatorUserId(operatorUserId);
        log.setAction(action);
        log.setReason(reason);
        log.setDurationHours(durationHours);
        log.setExpiresAt(expiresAt);
        log.setStatus(1);
        log.setCreatedAt(now);
        log.setUpdatedAt(now);
        userPunishLogMapper.insert(log);
    }
}
