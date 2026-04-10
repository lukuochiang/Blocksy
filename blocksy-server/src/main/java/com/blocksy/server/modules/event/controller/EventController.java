package com.blocksy.server.modules.event.controller;

import com.blocksy.server.common.api.ApiResponse;
import com.blocksy.server.modules.event.dto.EventRequest;
import com.blocksy.server.modules.event.dto.EventResponse;
import com.blocksy.server.modules.event.dto.EventSignupRequest;
import com.blocksy.server.modules.event.dto.EventSignupResponse;
import com.blocksy.server.modules.event.service.EventService;
import com.blocksy.server.security.SecurityUtils;
import com.blocksy.server.security.jwt.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@Tag(name = "Event", description = "活动接口")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    @Operation(summary = "活动列表")
    public ApiResponse<List<EventResponse>> list(@RequestParam(value = "communityId", required = false) Long communityId) {
        return ApiResponse.success(eventService.list(communityId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "活动详情")
    public ApiResponse<EventResponse> detail(@PathVariable("id") Long id) {
        return ApiResponse.success(eventService.getById(id));
    }

    @GetMapping("/mine")
    @Operation(summary = "我发布的活动")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<List<EventResponse>> listMine(@RequestParam(value = "communityId", required = false) Long communityId) {
        AuthenticatedUser currentUser = SecurityUtils.getCurrentUserOrThrow();
        return ApiResponse.success(eventService.listMine(currentUser.userId(), communityId));
    }

    @GetMapping("/my-signups")
    @Operation(summary = "我报名的活动")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<List<EventResponse>> listMySignups() {
        AuthenticatedUser currentUser = SecurityUtils.getCurrentUserOrThrow();
        return ApiResponse.success(eventService.listMySignups(currentUser.userId()));
    }

    @PostMapping
    @Operation(summary = "发布活动")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<EventResponse> create(@Valid @RequestBody EventRequest request) {
        AuthenticatedUser currentUser = SecurityUtils.getCurrentUserOrThrow();
        return ApiResponse.success(eventService.create(currentUser.userId(), request));
    }

    @PostMapping("/{id}/signup")
    @Operation(summary = "报名活动")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<EventSignupResponse> signup(@PathVariable("id") Long id, @RequestBody(required = false) EventSignupRequest request) {
        AuthenticatedUser currentUser = SecurityUtils.getCurrentUserOrThrow();
        return ApiResponse.success(eventService.signup(currentUser.userId(), id, request == null ? new EventSignupRequest(null) : request));
    }
}
