package com.blocksy.server.modules.event.service;

import com.blocksy.server.modules.event.dto.EventRequest;
import com.blocksy.server.modules.event.dto.EventResponse;
import com.blocksy.server.modules.event.dto.EventSignupRequest;
import com.blocksy.server.modules.event.dto.EventSignupResponse;

import java.util.List;

public interface EventService {
    List<EventResponse> list();

    EventResponse create(Long userId, EventRequest request);

    EventSignupResponse signup(Long userId, Long eventId, EventSignupRequest request);
}
