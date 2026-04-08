package com.blocksy.server.modules.event.controller;

import com.blocksy.server.common.api.ApiResponse;
import com.blocksy.server.modules.event.dto.EventRequest;
import com.blocksy.server.modules.event.dto.EventResponse;
import com.blocksy.server.modules.event.dto.EventSignupRequest;
import com.blocksy.server.modules.event.dto.EventSignupResponse;
import com.blocksy.server.modules.event.service.EventService;
import com.blocksy.server.security.SecurityUtils;
import com.blocksy.server.security.jwt.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public ApiResponse<List<EventResponse>> list() {
        return ApiResponse.success(eventService.list());
    }

    @PostMapping
    public ApiResponse<EventResponse> create(@Valid @RequestBody EventRequest request) {
        AuthenticatedUser currentUser = SecurityUtils.getCurrentUserOrThrow();
        return ApiResponse.success(eventService.create(currentUser.userId(), request));
    }

    @PostMapping("/{id}/signup")
    public ApiResponse<EventSignupResponse> signup(@PathVariable("id") Long id, @RequestBody(required = false) EventSignupRequest request) {
        AuthenticatedUser currentUser = SecurityUtils.getCurrentUserOrThrow();
        return ApiResponse.success(eventService.signup(currentUser.userId(), id, request == null ? new EventSignupRequest(null) : request));
    }
}
