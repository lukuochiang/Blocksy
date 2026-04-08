package com.blocksy.server.modules.user.service;

import com.blocksy.server.modules.user.dto.UserCommunityItemResponse;
import com.blocksy.server.modules.user.entity.UserEntity;

import java.util.List;

public interface UserService {
    UserEntity findByUsername(String username);

    UserEntity findById(Long id);

    List<UserEntity> listActiveUsers();

    void banUser(Long userId);

    void unbanUser(Long userId);

    boolean isUserActive(Long userId);

    List<UserCommunityItemResponse> listUserCommunities(Long userId);

    Long getDefaultCommunityId(Long userId);

    void selectDefaultCommunity(Long userId, Long communityId);
}
