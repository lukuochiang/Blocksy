package com.blocksy.server.modules.event.service;

import com.blocksy.server.modules.event.dto.EventRequest;
import com.blocksy.server.modules.event.dto.EventResponse;
import com.blocksy.server.modules.event.dto.EventSignupRequest;
import com.blocksy.server.modules.event.dto.EventSignupResponse;
import com.blocksy.server.modules.event.dto.AdminEventHandleLogResponse;
import com.blocksy.server.modules.event.dto.AdminEventBatchHandleRequest;
import com.blocksy.server.modules.event.dto.AdminEventBatchHandleResponse;
import com.blocksy.server.modules.event.dto.AdminEventBatchRetryRequest;
import com.blocksy.server.modules.event.dto.AdminEventResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    List<EventResponse> list(Long communityId);

    EventResponse getById(Long id);

    List<EventResponse> listMine(Long userId, Long communityId);

    List<EventResponse> listMySignups(Long userId);

    EventResponse create(Long userId, EventRequest request);

    EventSignupResponse signup(Long userId, Long eventId, EventSignupRequest request);

    List<AdminEventResponse> listForAdmin(Integer status, Long communityId, String keyword);

    AdminEventResponse handleForAdmin(Long eventId, Long operatorUserId, String action, String note);

    List<AdminEventHandleLogResponse> listHandleLogsForAdmin(Long eventId);

    List<AdminEventHandleLogResponse> listHandleLogsForAdmin(
            Long eventId,
            Long operatorUserId,
            String action,
            LocalDateTime startAt,
            LocalDateTime endAt
    );

    String exportHandleLogsCsv(
            Long eventId,
            Long operatorUserId,
            String action,
            LocalDateTime startAt,
            LocalDateTime endAt
    );

    AdminEventBatchHandleResponse batchHandleForAdmin(Long operatorUserId, AdminEventBatchHandleRequest request);

    AdminEventBatchHandleResponse batchRetryForAdmin(Long operatorUserId, AdminEventBatchRetryRequest request);

    String exportBatchHandleResultCsv(AdminEventBatchHandleResponse response);
}
