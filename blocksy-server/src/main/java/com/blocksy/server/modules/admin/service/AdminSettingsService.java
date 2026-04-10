package com.blocksy.server.modules.admin.service;

import com.blocksy.server.modules.admin.dto.PlatformSettingResponse;
import com.blocksy.server.modules.admin.dto.PlatformSettingSaveRequest;
import com.blocksy.server.modules.admin.dto.PolicyDocumentResponse;
import com.blocksy.server.modules.admin.dto.PolicyDocumentSaveRequest;

import java.util.List;

public interface AdminSettingsService {

    List<PlatformSettingResponse> listSettings(String settingGroup);

    PlatformSettingResponse saveSetting(Long adminUserId, PlatformSettingSaveRequest request);

    List<PolicyDocumentResponse> listPolicies(String policyType);

    PolicyDocumentResponse savePolicy(Long adminUserId, PolicyDocumentSaveRequest request);

    void activatePolicy(Long adminUserId, Long id);
}
