package com.blocksy.server.modules.comment.service;

import com.blocksy.server.modules.comment.dto.CommentRequest;
import com.blocksy.server.modules.comment.dto.CommentResponse;
import com.blocksy.server.modules.comment.dto.AdminCommentResponse;

import java.util.List;

public interface CommentService {
    CommentResponse create(Long userId, CommentRequest request);

    List<CommentResponse> listByPostId(Long postId);

    List<AdminCommentResponse> listForAdmin(Long postId, Integer status, String keyword);

    void deleteForAdmin(Long commentId);
}
