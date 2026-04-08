package com.blocksy.server.modules.listing.controller;

import com.blocksy.server.common.api.ApiResponse;
import com.blocksy.server.modules.listing.dto.ListingRequest;
import com.blocksy.server.modules.listing.dto.ListingResponse;
import com.blocksy.server.modules.listing.service.ListingService;
import com.blocksy.server.security.SecurityUtils;
import com.blocksy.server.security.jwt.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/listings")
public class ListingController {

    private final ListingService listingService;

    public ListingController(ListingService listingService) {
        this.listingService = listingService;
    }

    @GetMapping
    public ApiResponse<List<ListingResponse>> list() {
        return ApiResponse.success(listingService.list());
    }

    @PostMapping
    public ApiResponse<ListingResponse> create(@Valid @RequestBody ListingRequest request) {
        AuthenticatedUser currentUser = SecurityUtils.getCurrentUserOrThrow();
        return ApiResponse.success(listingService.create(currentUser.userId(), request));
    }
}
