package com.blocksy.server.modules.post.service;

import com.blocksy.server.modules.post.dto.PostRequest;
import com.blocksy.server.modules.post.dto.PostResponse;
import com.blocksy.server.modules.post.dto.AdminPostResponse;

import java.util.List;

public interface PostService {
    List<PostResponse> list(Long communityId);

    List<PostResponse> listMine(Long userId, Long communityId);

    PostResponse getById(Long id);

    PostResponse create(Long userId, PostRequest request);

    List<AdminPostResponse> listForAdmin(Integer status, Long communityId, String keyword);

    AdminPostResponse reviewForAdmin(Long postId, String action);
}
