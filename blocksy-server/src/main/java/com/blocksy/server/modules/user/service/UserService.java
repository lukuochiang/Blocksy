package com.blocksy.server.modules.user.service;

import com.blocksy.server.modules.user.dto.AdminUserPunishLogResponse;
import com.blocksy.server.modules.user.dto.UserCommunityItemResponse;
import com.blocksy.server.modules.user.entity.UserEntity;

import java.util.List;

public interface UserService {
    UserEntity findByUsername(String username);

    UserEntity findById(Long id);

    List<UserEntity> listActiveUsers();

    List<UserEntity> listBlacklistedUsers();

    void banUser(Long userId);

    void banUser(Long userId, Long operatorUserId, String reason, Integer durationHours);

    void unbanUser(Long userId);

    void unbanUser(Long userId, Long operatorUserId, String reason);

    boolean isUserActive(Long userId);

    List<AdminUserPunishLogResponse> listPunishLogs(Long userId);

    List<UserCommunityItemResponse> listUserCommunities(Long userId);

    Long getDefaultCommunityId(Long userId);

    void selectDefaultCommunity(Long userId, Long communityId);
}
