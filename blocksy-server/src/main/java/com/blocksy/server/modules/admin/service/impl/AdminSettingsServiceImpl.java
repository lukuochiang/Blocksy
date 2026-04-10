package com.blocksy.server.modules.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.blocksy.server.common.exception.BusinessException;
import com.blocksy.server.modules.admin.dto.PlatformSettingResponse;
import com.blocksy.server.modules.admin.dto.PlatformSettingSaveRequest;
import com.blocksy.server.modules.admin.dto.PolicyDocumentResponse;
import com.blocksy.server.modules.admin.dto.PolicyDocumentSaveRequest;
import com.blocksy.server.modules.admin.entity.AdminOperationLogEntity;
import com.blocksy.server.modules.admin.entity.PlatformSettingEntity;
import com.blocksy.server.modules.admin.entity.PolicyDocumentEntity;
import com.blocksy.server.modules.admin.mapper.AdminOperationLogMapper;
import com.blocksy.server.modules.admin.mapper.PlatformSettingMapper;
import com.blocksy.server.modules.admin.mapper.PolicyDocumentMapper;
import com.blocksy.server.modules.admin.service.AdminSettingsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminSettingsServiceImpl implements AdminSettingsService {

    private final PlatformSettingMapper platformSettingMapper;
    private final PolicyDocumentMapper policyDocumentMapper;
    private final AdminOperationLogMapper adminOperationLogMapper;

    public AdminSettingsServiceImpl(
            PlatformSettingMapper platformSettingMapper,
            PolicyDocumentMapper policyDocumentMapper,
            AdminOperationLogMapper adminOperationLogMapper
    ) {
        this.platformSettingMapper = platformSettingMapper;
        this.policyDocumentMapper = policyDocumentMapper;
        this.adminOperationLogMapper = adminOperationLogMapper;
    }

    @Override
    public List<PlatformSettingResponse> listSettings(String settingGroup) {
        LambdaQueryWrapper<PlatformSettingEntity> query = new LambdaQueryWrapper<PlatformSettingEntity>()
                .eq(PlatformSettingEntity::getStatus, 1)
                .orderByAsc(PlatformSettingEntity::getSettingKey);
        if (settingGroup != null && !settingGroup.isBlank()) {
            query.eq(PlatformSettingEntity::getSettingGroup, settingGroup.trim().toUpperCase());
        }
        return platformSettingMapper.selectList(query).stream()
                .map(item -> new PlatformSettingResponse(
                        item.getId(),
                        item.getSettingGroup(),
                        item.getSettingKey(),
                        item.getSettingValue(),
                        item.getDescription(),
                        item.getUpdatedAt()
                )).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PlatformSettingResponse saveSetting(Long adminUserId, PlatformSettingSaveRequest request) {
        String group = request.settingGroup().trim().toUpperCase();
        String key = request.settingKey().trim();
        LocalDateTime now = LocalDateTime.now();
        PlatformSettingEntity existing = platformSettingMapper.selectOne(
                new LambdaQueryWrapper<PlatformSettingEntity>()
                        .eq(PlatformSettingEntity::getSettingGroup, group)
                        .eq(PlatformSettingEntity::getSettingKey, key)
                        .last("LIMIT 1")
        );
        if (existing == null) {
            PlatformSettingEntity entity = new PlatformSettingEntity();
            entity.setSettingGroup(group);
            entity.setSettingKey(key);
            entity.setSettingValue(request.settingValue());
            entity.setDescription(request.description());
            entity.setStatus(1);
            entity.setCreatedAt(now);
            entity.setUpdatedAt(now);
            platformSettingMapper.insert(entity);
            appendLog(adminUserId, "SETTINGS", "CREATE_SETTING", "PLATFORM_SETTING", entity.getId(), group + ":" + key);
            return new PlatformSettingResponse(
                    entity.getId(),
                    entity.getSettingGroup(),
                    entity.getSettingKey(),
                    entity.getSettingValue(),
                    entity.getDescription(),
                    entity.getUpdatedAt()
            );
        }
        existing.setSettingValue(request.settingValue());
        existing.setDescription(request.description());
        existing.setStatus(1);
        existing.setUpdatedAt(now);
        platformSettingMapper.updateById(existing);
        appendLog(adminUserId, "SETTINGS", "UPDATE_SETTING", "PLATFORM_SETTING", existing.getId(), group + ":" + key);
        return new PlatformSettingResponse(
                existing.getId(),
                existing.getSettingGroup(),
                existing.getSettingKey(),
                existing.getSettingValue(),
                existing.getDescription(),
                existing.getUpdatedAt()
        );
    }

    @Override
    public List<PolicyDocumentResponse> listPolicies(String policyType) {
        LambdaQueryWrapper<PolicyDocumentEntity> query = new LambdaQueryWrapper<PolicyDocumentEntity>()
                .eq(PolicyDocumentEntity::getStatus, 1)
                .orderByDesc(PolicyDocumentEntity::getCreatedAt);
        if (policyType != null && !policyType.isBlank()) {
            query.eq(PolicyDocumentEntity::getPolicyType, policyType.trim().toUpperCase());
        }
        return policyDocumentMapper.selectList(query).stream().map(this::toPolicyResponse).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PolicyDocumentResponse savePolicy(Long adminUserId, PolicyDocumentSaveRequest request) {
        LocalDateTime now = LocalDateTime.now();
        PolicyDocumentEntity entity = new PolicyDocumentEntity();
        entity.setPolicyType(request.policyType().trim().toUpperCase());
        entity.setVersion(request.version().trim());
        entity.setTitle(request.title().trim());
        entity.setContent(request.content().trim());
        entity.setActive(false);
        entity.setStatus(1);
        entity.setCreatedBy(adminUserId);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        policyDocumentMapper.insert(entity);
        appendLog(adminUserId, "SETTINGS", "CREATE_POLICY", "POLICY_DOCUMENT", entity.getId(), entity.getPolicyType() + ":" + entity.getVersion());
        return toPolicyResponse(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void activatePolicy(Long adminUserId, Long id) {
        PolicyDocumentEntity row = policyDocumentMapper.selectById(id);
        if (row == null || row.getStatus() == null || row.getStatus() != 1) {
            throw new BusinessException("政策文档不存在");
        }
        LocalDateTime now = LocalDateTime.now();
        policyDocumentMapper.update(
                null,
                new LambdaUpdateWrapper<PolicyDocumentEntity>()
                        .eq(PolicyDocumentEntity::getPolicyType, row.getPolicyType())
                        .eq(PolicyDocumentEntity::getStatus, 1)
                        .set(PolicyDocumentEntity::getActive, false)
                        .set(PolicyDocumentEntity::getUpdatedAt, now)
        );
        row.setActive(true);
        row.setPublishedAt(now);
        row.setUpdatedAt(now);
        policyDocumentMapper.updateById(row);
        appendLog(adminUserId, "SETTINGS", "ACTIVATE_POLICY", "POLICY_DOCUMENT", row.getId(), row.getPolicyType() + ":" + row.getVersion());
    }

    private PolicyDocumentResponse toPolicyResponse(PolicyDocumentEntity entity) {
        return new PolicyDocumentResponse(
                entity.getId(),
                entity.getPolicyType(),
                entity.getVersion(),
                entity.getTitle(),
                entity.getContent(),
                entity.getActive(),
                entity.getCreatedBy(),
                entity.getPublishedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private void appendLog(Long adminUserId, String module, String action, String targetType, Long targetId, String details) {
        LocalDateTime now = LocalDateTime.now();
        AdminOperationLogEntity log = new AdminOperationLogEntity();
        log.setModule(module);
        log.setAction(action);
        log.setOperatorUserId(adminUserId);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setDetails(details);
        log.setStatus(1);
        log.setCreatedAt(now);
        log.setUpdatedAt(now);
        adminOperationLogMapper.insert(log);
    }
}
