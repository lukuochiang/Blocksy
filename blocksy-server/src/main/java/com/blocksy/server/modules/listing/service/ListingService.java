package com.blocksy.server.modules.listing.service;

import com.blocksy.server.modules.listing.dto.ListingRequest;
import com.blocksy.server.modules.listing.dto.ListingResponse;

import java.util.List;

public interface ListingService {
    List<ListingResponse> list();

    ListingResponse create(Long userId, ListingRequest request);
}
