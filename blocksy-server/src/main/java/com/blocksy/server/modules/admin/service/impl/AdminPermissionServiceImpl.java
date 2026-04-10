package com.blocksy.server.modules.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blocksy.server.common.api.PageResponse;
import com.blocksy.server.modules.admin.dto.AdminOperationLogResponse;
import com.blocksy.server.modules.admin.dto.DataPermissionAssignRequest;
import com.blocksy.server.modules.admin.dto.DataPermissionItemResponse;
import com.blocksy.server.modules.admin.dto.MenuPermissionAssignRequest;
import com.blocksy.server.modules.admin.dto.MenuPermissionItemResponse;
import com.blocksy.server.modules.admin.dto.PermissionOpLogResponse;
import com.blocksy.server.modules.admin.dto.UserBehaviorLogResponse;
import com.blocksy.server.modules.admin.entity.AdminOperationLogEntity;
import com.blocksy.server.modules.admin.entity.DataPermissionEntity;
import com.blocksy.server.modules.admin.entity.MenuPermissionEntity;
import com.blocksy.server.modules.admin.entity.PermissionOpLogEntity;
import com.blocksy.server.modules.admin.entity.UserBehaviorLogEntity;
import com.blocksy.server.modules.admin.mapper.AdminOperationLogMapper;
import com.blocksy.server.modules.admin.mapper.DataPermissionMapper;
import com.blocksy.server.modules.admin.mapper.MenuPermissionMapper;
import com.blocksy.server.modules.admin.mapper.PermissionOpLogMapper;
import com.blocksy.server.modules.admin.mapper.UserBehaviorLogMapper;
import com.blocksy.server.modules.admin.service.AdminPermissionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class AdminPermissionServiceImpl implements AdminPermissionService {

    private final MenuPermissionMapper menuPermissionMapper;
    private final PermissionOpLogMapper permissionOpLogMapper;
    private final DataPermissionMapper dataPermissionMapper;
    private final AdminOperationLogMapper adminOperationLogMapper;
    private final UserBehaviorLogMapper userBehaviorLogMapper;

    public AdminPermissionServiceImpl(
            MenuPermissionMapper menuPermissionMapper,
            PermissionOpLogMapper permissionOpLogMapper,
            DataPermissionMapper dataPermissionMapper,
            AdminOperationLogMapper adminOperationLogMapper,
            UserBehaviorLogMapper userBehaviorLogMapper
    ) {
        this.menuPermissionMapper = menuPermissionMapper;
        this.permissionOpLogMapper = permissionOpLogMapper;
        this.dataPermissionMapper = dataPermissionMapper;
        this.adminOperationLogMapper = adminOperationLogMapper;
        this.userBehaviorLogMapper = userBehaviorLogMapper;
    }

    @Override
    public PageResponse<MenuPermissionItemResponse> pageMenuPermissions(String roleCode, String keyword, Integer page, Integer pageSize) {
        int currentPage = resolvePage(page);
        int size = resolvePageSize(pageSize, 200);
        LambdaQueryWrapper<MenuPermissionEntity> countQuery = new LambdaQueryWrapper<MenuPermissionEntity>()
                .eq(MenuPermissionEntity::getStatus, 1);
        if (roleCode != null && !roleCode.isBlank()) {
            countQuery.eq(MenuPermissionEntity::getRoleCode, roleCode.trim().toUpperCase());
        }
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            countQuery.and(w -> w.like(MenuPermissionEntity::getMenuName, kw)
                    .or()
                    .like(MenuPermissionEntity::getMenuKey, kw)
                    .or()
                    .like(MenuPermissionEntity::getMenuPath, kw));
        }
        long total = menuPermissionMapper.selectCount(countQuery);
        if (total == 0) {
            return new PageResponse<>(currentPage, size, 0L, Collections.emptyList());
        }
        LambdaQueryWrapper<MenuPermissionEntity> pageQuery = new LambdaQueryWrapper<MenuPermissionEntity>()
                .eq(MenuPermissionEntity::getStatus, 1)
                .orderByAsc(MenuPermissionEntity::getRoleCode)
                .orderByAsc(MenuPermissionEntity::getMenuKey)
                .last("LIMIT " + size + " OFFSET " + ((currentPage - 1) * size));
        if (roleCode != null && !roleCode.isBlank()) {
            pageQuery.eq(MenuPermissionEntity::getRoleCode, roleCode.trim().toUpperCase());
        }
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            pageQuery.and(w -> w.like(MenuPermissionEntity::getMenuName, kw)
                    .or()
                    .like(MenuPermissionEntity::getMenuKey, kw)
                    .or()
                    .like(MenuPermissionEntity::getMenuPath, kw));
        }
        List<MenuPermissionItemResponse> items = menuPermissionMapper.selectList(pageQuery).stream().map(this::toResponse).toList();
        return new PageResponse<>(currentPage, size, total, items);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int assignRoleMenus(Long adminUserId, MenuPermissionAssignRequest request) {
        String role = request.roleCode().trim().toUpperCase();
        List<String> menuKeys = request.menuKeys().stream().map(String::trim).filter(k -> !k.isEmpty()).distinct().toList();
        if (menuKeys.isEmpty()) {
            return 0;
        }
        LocalDateTime now = LocalDateTime.now();
        int updated = 0;
        for (String menuKey : menuKeys) {
            MenuPermissionEntity existing = menuPermissionMapper.selectOne(
                    new LambdaQueryWrapper<MenuPermissionEntity>()
                            .eq(MenuPermissionEntity::getRoleCode, role)
                            .eq(MenuPermissionEntity::getMenuKey, menuKey)
                            .last("LIMIT 1")
            );
            if (existing == null) {
                MenuPermissionEntity entity = new MenuPermissionEntity();
                entity.setRoleCode(role);
                entity.setMenuKey(menuKey);
                entity.setMenuName(menuKey);
                entity.setMenuPath("/" + menuKey.replace(".", "/"));
                entity.setEnabled(true);
                entity.setStatus(1);
                entity.setCreatedAt(now);
                entity.setUpdatedAt(now);
                menuPermissionMapper.insert(entity);
            } else {
                existing.setEnabled(true);
                existing.setStatus(1);
                existing.setUpdatedAt(now);
                menuPermissionMapper.updateById(existing);
            }
            updated++;
        }
        PermissionOpLogEntity log = new PermissionOpLogEntity();
        log.setRoleCode(role);
        log.setOperatorUserId(adminUserId);
        log.setAction("ASSIGN_ROLE_MENUS");
        log.setDetails("menus=" + String.join(",", menuKeys));
        log.setStatus(1);
        log.setCreatedAt(now);
        log.setUpdatedAt(now);
        permissionOpLogMapper.insert(log);
        appendAdminOperationLog("PERMISSION", "ASSIGN_ROLE_MENUS", adminUserId, "ROLE", null, "role=" + role + ",menus=" + String.join(",", menuKeys));
        return updated;
    }

    @Override
    public List<PermissionOpLogResponse> listPermissionLogs(String roleCode, Integer limit) {
        int resolvedLimit = limit == null || limit < 1 ? 50 : Math.min(limit, 200);
        LambdaQueryWrapper<PermissionOpLogEntity> query = new LambdaQueryWrapper<PermissionOpLogEntity>()
                .eq(PermissionOpLogEntity::getStatus, 1)
                .orderByDesc(PermissionOpLogEntity::getCreatedAt)
                .last("LIMIT " + resolvedLimit);
        if (roleCode != null && !roleCode.isBlank()) {
            query.eq(PermissionOpLogEntity::getRoleCode, roleCode.trim().toUpperCase());
        }
        List<PermissionOpLogResponse> rows = new ArrayList<>();
        for (PermissionOpLogEntity item : permissionOpLogMapper.selectList(query)) {
            rows.add(new PermissionOpLogResponse(
                    item.getId(),
                    item.getRoleCode(),
                    item.getOperatorUserId(),
                    item.getAction(),
                    item.getDetails(),
                    item.getCreatedAt()
            ));
        }
        return rows;
    }

    @Override
    public PageResponse<DataPermissionItemResponse> pageDataPermissions(String roleCode, String dataScope, Integer page, Integer pageSize) {
        int currentPage = resolvePage(page);
        int size = resolvePageSize(pageSize, 200);
        LambdaQueryWrapper<DataPermissionEntity> countQuery = new LambdaQueryWrapper<DataPermissionEntity>()
                .eq(DataPermissionEntity::getStatus, 1);
        if (roleCode != null && !roleCode.isBlank()) {
            countQuery.eq(DataPermissionEntity::getRoleCode, roleCode.trim().toUpperCase());
        }
        if (dataScope != null && !dataScope.isBlank()) {
            countQuery.eq(DataPermissionEntity::getDataScope, dataScope.trim().toUpperCase());
        }
        long total = dataPermissionMapper.selectCount(countQuery);
        if (total == 0) {
            return new PageResponse<>(currentPage, size, 0L, Collections.emptyList());
        }
        LambdaQueryWrapper<DataPermissionEntity> pageQuery = new LambdaQueryWrapper<DataPermissionEntity>()
                .eq(DataPermissionEntity::getStatus, 1)
                .orderByAsc(DataPermissionEntity::getRoleCode)
                .orderByAsc(DataPermissionEntity::getDataScope)
                .orderByAsc(DataPermissionEntity::getDataValue)
                .last("LIMIT " + size + " OFFSET " + ((currentPage - 1) * size));
        if (roleCode != null && !roleCode.isBlank()) {
            pageQuery.eq(DataPermissionEntity::getRoleCode, roleCode.trim().toUpperCase());
        }
        if (dataScope != null && !dataScope.isBlank()) {
            pageQuery.eq(DataPermissionEntity::getDataScope, dataScope.trim().toUpperCase());
        }
        List<DataPermissionItemResponse> items = dataPermissionMapper.selectList(pageQuery).stream()
                .map(item -> new DataPermissionItemResponse(
                        item.getId(),
                        item.getRoleCode(),
                        item.getDataScope(),
                        item.getDataValue(),
                        item.getEnabled(),
                        item.getUpdatedAt()
                )).toList();
        return new PageResponse<>(currentPage, size, total, items);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int assignDataPermissions(Long adminUserId, DataPermissionAssignRequest request) {
        String role = request.roleCode().trim().toUpperCase();
        String scope = request.dataScope().trim().toUpperCase();
        List<String> values = request.dataValues().stream().map(String::trim).filter(v -> !v.isEmpty()).distinct().toList();
        if (values.isEmpty()) {
            return 0;
        }
        LocalDateTime now = LocalDateTime.now();
        int affected = 0;
        for (String value : values) {
            DataPermissionEntity existing = dataPermissionMapper.selectOne(
                    new LambdaQueryWrapper<DataPermissionEntity>()
                            .eq(DataPermissionEntity::getRoleCode, role)
                            .eq(DataPermissionEntity::getDataScope, scope)
                            .eq(DataPermissionEntity::getDataValue, value)
                            .last("LIMIT 1")
            );
            if (existing == null) {
                DataPermissionEntity row = new DataPermissionEntity();
                row.setRoleCode(role);
                row.setDataScope(scope);
                row.setDataValue(value);
                row.setEnabled(true);
                row.setStatus(1);
                row.setCreatedAt(now);
                row.setUpdatedAt(now);
                dataPermissionMapper.insert(row);
            } else {
                existing.setEnabled(true);
                existing.setStatus(1);
                existing.setUpdatedAt(now);
                dataPermissionMapper.updateById(existing);
            }
            affected++;
        }
        appendAdminOperationLog(
                "PERMISSION",
                "ASSIGN_DATA_PERMISSION",
                adminUserId,
                "ROLE",
                null,
                "role=" + role + ",scope=" + scope + ",values=" + String.join(",", values)
        );
        return affected;
    }

    @Override
    public PageResponse<AdminOperationLogResponse> pageOperationLogs(String module, String action, Integer page, Integer pageSize) {
        int currentPage = resolvePage(page);
        int size = resolvePageSize(pageSize, 200);
        LambdaQueryWrapper<AdminOperationLogEntity> countQuery = new LambdaQueryWrapper<AdminOperationLogEntity>()
                .eq(AdminOperationLogEntity::getStatus, 1);
        if (module != null && !module.isBlank()) {
            countQuery.eq(AdminOperationLogEntity::getModule, module.trim().toUpperCase());
        }
        if (action != null && !action.isBlank()) {
            countQuery.eq(AdminOperationLogEntity::getAction, action.trim().toUpperCase());
        }
        long total = adminOperationLogMapper.selectCount(countQuery);
        if (total == 0) {
            return new PageResponse<>(currentPage, size, 0L, Collections.emptyList());
        }
        LambdaQueryWrapper<AdminOperationLogEntity> pageQuery = new LambdaQueryWrapper<AdminOperationLogEntity>()
                .eq(AdminOperationLogEntity::getStatus, 1)
                .orderByDesc(AdminOperationLogEntity::getCreatedAt)
                .last("LIMIT " + size + " OFFSET " + ((currentPage - 1) * size));
        if (module != null && !module.isBlank()) {
            pageQuery.eq(AdminOperationLogEntity::getModule, module.trim().toUpperCase());
        }
        if (action != null && !action.isBlank()) {
            pageQuery.eq(AdminOperationLogEntity::getAction, action.trim().toUpperCase());
        }
        List<AdminOperationLogResponse> items = adminOperationLogMapper.selectList(pageQuery).stream().map(item ->
                new AdminOperationLogResponse(
                        item.getId(),
                        item.getModule(),
                        item.getAction(),
                        item.getOperatorUserId(),
                        item.getTargetType(),
                        item.getTargetId(),
                        item.getDetails(),
                        item.getCreatedAt()
                )).toList();
        return new PageResponse<>(currentPage, size, total, items);
    }

    @Override
    public PageResponse<UserBehaviorLogResponse> pageUserBehaviorLogs(Long userId, String behaviorType, Integer page, Integer pageSize) {
        int currentPage = resolvePage(page);
        int size = resolvePageSize(pageSize, 200);
        LambdaQueryWrapper<UserBehaviorLogEntity> countQuery = new LambdaQueryWrapper<UserBehaviorLogEntity>()
                .eq(UserBehaviorLogEntity::getStatus, 1);
        if (userId != null) {
            countQuery.eq(UserBehaviorLogEntity::getUserId, userId);
        }
        if (behaviorType != null && !behaviorType.isBlank()) {
            countQuery.eq(UserBehaviorLogEntity::getBehaviorType, behaviorType.trim().toUpperCase());
        }
        long total = userBehaviorLogMapper.selectCount(countQuery);
        if (total == 0) {
            return new PageResponse<>(currentPage, size, 0L, Collections.emptyList());
        }
        LambdaQueryWrapper<UserBehaviorLogEntity> pageQuery = new LambdaQueryWrapper<UserBehaviorLogEntity>()
                .eq(UserBehaviorLogEntity::getStatus, 1)
                .orderByDesc(UserBehaviorLogEntity::getCreatedAt)
                .last("LIMIT " + size + " OFFSET " + ((currentPage - 1) * size));
        if (userId != null) {
            pageQuery.eq(UserBehaviorLogEntity::getUserId, userId);
        }
        if (behaviorType != null && !behaviorType.isBlank()) {
            pageQuery.eq(UserBehaviorLogEntity::getBehaviorType, behaviorType.trim().toUpperCase());
        }
        List<UserBehaviorLogResponse> items = userBehaviorLogMapper.selectList(pageQuery).stream().map(item ->
                new UserBehaviorLogResponse(
                        item.getId(),
                        item.getUserId(),
                        item.getBehaviorType(),
                        item.getResourceType(),
                        item.getResourceId(),
                        item.getIp(),
                        item.getDevice(),
                        item.getCreatedAt()
                )).toList();
        return new PageResponse<>(currentPage, size, total, items);
    }

    private void appendAdminOperationLog(String module, String action, Long operatorUserId, String targetType, Long targetId, String details) {
        LocalDateTime now = LocalDateTime.now();
        AdminOperationLogEntity log = new AdminOperationLogEntity();
        log.setModule(module == null ? "UNKNOWN" : module.toUpperCase());
        log.setAction(action == null ? "UNKNOWN" : action.toUpperCase());
        log.setOperatorUserId(operatorUserId);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setDetails(details);
        log.setStatus(1);
        log.setCreatedAt(now);
        log.setUpdatedAt(now);
        adminOperationLogMapper.insert(log);
    }

    private MenuPermissionItemResponse toResponse(MenuPermissionEntity row) {
        return new MenuPermissionItemResponse(
                row.getId(),
                row.getRoleCode(),
                row.getMenuKey(),
                row.getMenuName(),
                row.getMenuPath(),
                row.getEnabled(),
                row.getUpdatedAt()
        );
    }

    private int resolvePage(Integer page) {
        return page == null || page < 1 ? 1 : page;
    }

    private int resolvePageSize(Integer pageSize, int max) {
        if (pageSize == null || pageSize < 1) {
            return 10;
        }
        return Math.min(pageSize, max);
    }
}
