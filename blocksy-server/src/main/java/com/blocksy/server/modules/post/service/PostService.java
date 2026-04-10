package com.blocksy.server.modules.post.service;

import com.blocksy.server.common.api.PageResponse;
import com.blocksy.server.modules.post.dto.PostRequest;
import com.blocksy.server.modules.post.dto.PostResponse;
import com.blocksy.server.modules.post.dto.AdminPostResponse;

public interface PostService {
    PageResponse<PostResponse> list(Long communityId, String keyword, Integer page, Integer pageSize);

    PageResponse<PostResponse> listMine(Long userId, Long communityId, String keyword, Integer page, Integer pageSize);

    PostResponse getById(Long id);

    PostResponse create(Long userId, PostRequest request);

    PageResponse<AdminPostResponse> listForAdmin(Integer status, Long communityId, String keyword, Integer page, Integer pageSize);

    AdminPostResponse reviewForAdmin(Long postId, String action);
}
