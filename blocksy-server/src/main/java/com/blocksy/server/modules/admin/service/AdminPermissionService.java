package com.blocksy.server.modules.admin.service;

import com.blocksy.server.common.api.PageResponse;
import com.blocksy.server.modules.admin.dto.AdminOperationLogResponse;
import com.blocksy.server.modules.admin.dto.DataPermissionAssignRequest;
import com.blocksy.server.modules.admin.dto.DataPermissionItemResponse;
import com.blocksy.server.modules.admin.dto.MenuPermissionAssignRequest;
import com.blocksy.server.modules.admin.dto.MenuPermissionItemResponse;
import com.blocksy.server.modules.admin.dto.PermissionOpLogResponse;
import com.blocksy.server.modules.admin.dto.UserBehaviorLogResponse;

import java.util.List;

public interface AdminPermissionService {
    PageResponse<MenuPermissionItemResponse> pageMenuPermissions(String roleCode, String keyword, Integer page, Integer pageSize);

    int assignRoleMenus(Long adminUserId, MenuPermissionAssignRequest request);

    List<PermissionOpLogResponse> listPermissionLogs(String roleCode, Integer limit);

    PageResponse<DataPermissionItemResponse> pageDataPermissions(String roleCode, String dataScope, Integer page, Integer pageSize);

    int assignDataPermissions(Long adminUserId, DataPermissionAssignRequest request);

    PageResponse<AdminOperationLogResponse> pageOperationLogs(String module, String action, Integer page, Integer pageSize);

    PageResponse<UserBehaviorLogResponse> pageUserBehaviorLogs(Long userId, String behaviorType, Integer page, Integer pageSize);
}
