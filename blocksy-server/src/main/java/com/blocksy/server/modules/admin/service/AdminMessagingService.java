package com.blocksy.server.modules.admin.service;

import com.blocksy.server.common.api.PageResponse;
import com.blocksy.server.modules.admin.dto.NotificationTemplateResponse;
import com.blocksy.server.modules.admin.dto.NotificationTemplateSaveRequest;
import com.blocksy.server.modules.admin.dto.PushRecordResponse;
import com.blocksy.server.modules.admin.dto.PushTaskCreateRequest;
import com.blocksy.server.modules.admin.dto.PushTaskResponse;

public interface AdminMessagingService {

    PageResponse<PushTaskResponse> pagePushTasks(String taskStatus, String keyword, Integer page, Integer pageSize);

    PushTaskResponse createPushTask(Long adminUserId, PushTaskCreateRequest request);

    int sendPushTask(Long taskId, Long adminUserId);

    PageResponse<PushRecordResponse> pagePushRecords(Long taskId, String sendStatus, Integer page, Integer pageSize);

    PageResponse<NotificationTemplateResponse> pageNotificationTemplates(String module, Boolean enabled, Integer page, Integer pageSize);

    NotificationTemplateResponse saveNotificationTemplate(Long adminUserId, NotificationTemplateSaveRequest request);
}
