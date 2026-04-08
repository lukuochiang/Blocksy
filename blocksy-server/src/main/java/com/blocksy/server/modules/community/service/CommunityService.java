package com.blocksy.server.modules.community.service;

import com.blocksy.server.modules.community.dto.AdminCommunityCreateRequest;
import com.blocksy.server.modules.community.dto.CommunityResponse;
import com.blocksy.server.modules.community.entity.CommunityEntity;

import java.util.List;

public interface CommunityService {
    List<CommunityResponse> list();

    List<CommunityEntity> listForAdmin();

    CommunityEntity createForAdmin(AdminCommunityCreateRequest request);
}
