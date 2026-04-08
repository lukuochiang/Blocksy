package com.blocksy.server.modules.comment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blocksy.server.common.exception.BusinessException;
import com.blocksy.server.modules.comment.dto.AdminCommentResponse;
import com.blocksy.server.modules.comment.dto.CommentRequest;
import com.blocksy.server.modules.comment.dto.CommentResponse;
import com.blocksy.server.modules.comment.entity.PostCommentEntity;
import com.blocksy.server.modules.comment.mapper.PostCommentMapper;
import com.blocksy.server.modules.comment.service.CommentService;
import com.blocksy.server.modules.post.entity.PostEntity;
import com.blocksy.server.modules.post.mapper.PostMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    private final PostCommentMapper postCommentMapper;
    private final PostMapper postMapper;

    public CommentServiceImpl(PostCommentMapper postCommentMapper, PostMapper postMapper) {
        this.postCommentMapper = postCommentMapper;
        this.postMapper = postMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentResponse create(Long userId, CommentRequest request) {
        PostEntity post = postMapper.selectOne(
                new LambdaQueryWrapper<PostEntity>()
                        .eq(PostEntity::getId, request.postId())
                        .eq(PostEntity::getStatus, 1)
                        .last("LIMIT 1")
        );
        if (post == null) {
            throw new IllegalArgumentException("帖子不存在");
        }

        LocalDateTime now = LocalDateTime.now();
        PostCommentEntity entity = new PostCommentEntity();
        entity.setPostId(request.postId());
        entity.setUserId(userId);
        entity.setContent(request.content());
        entity.setReplyToCommentId(request.replyToCommentId());
        entity.setStatus(1);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        postCommentMapper.insert(entity);

        post.setCommentCount((post.getCommentCount() == null ? 0 : post.getCommentCount()) + 1);
        post.setUpdatedAt(now);
        postMapper.updateById(post);

        return new CommentResponse(entity.getId(), entity.getPostId(), entity.getUserId(), entity.getContent(), entity.getCreatedAt());
    }

    @Override
    public List<CommentResponse> listByPostId(Long postId) {
        return postCommentMapper.selectList(
                new LambdaQueryWrapper<PostCommentEntity>()
                        .eq(PostCommentEntity::getPostId, postId)
                        .eq(PostCommentEntity::getStatus, 1)
                        .orderByAsc(PostCommentEntity::getCreatedAt)
                        .last("LIMIT 200")
        ).stream().map(entity -> new CommentResponse(
                entity.getId(),
                entity.getPostId(),
                entity.getUserId(),
                entity.getContent(),
                entity.getCreatedAt()
        )).toList();
    }

    @Override
    public List<AdminCommentResponse> listForAdmin(Long postId, Integer status, String keyword) {
        LambdaQueryWrapper<PostCommentEntity> queryWrapper = new LambdaQueryWrapper<PostCommentEntity>()
                .orderByDesc(PostCommentEntity::getCreatedAt)
                .last("LIMIT 300");
        if (postId != null) {
            queryWrapper.eq(PostCommentEntity::getPostId, postId);
        }
        if (status != null) {
            queryWrapper.eq(PostCommentEntity::getStatus, status);
        }
        if (keyword != null && !keyword.isBlank()) {
            queryWrapper.like(PostCommentEntity::getContent, keyword.trim());
        }
        return postCommentMapper.selectList(queryWrapper).stream().map(this::toAdminResponse).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteForAdmin(Long commentId) {
        PostCommentEntity comment = postCommentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        if (comment.getStatus() != null && comment.getStatus() == 0) {
            return;
        }
        comment.setStatus(0);
        comment.setUpdatedAt(LocalDateTime.now());
        postCommentMapper.updateById(comment);

        PostEntity post = postMapper.selectById(comment.getPostId());
        if (post != null) {
            Integer commentCount = post.getCommentCount() == null ? 0 : post.getCommentCount();
            post.setCommentCount(Math.max(commentCount - 1, 0));
            post.setUpdatedAt(LocalDateTime.now());
            postMapper.updateById(post);
        }
    }

    private AdminCommentResponse toAdminResponse(PostCommentEntity entity) {
        return new AdminCommentResponse(
                entity.getId(),
                entity.getPostId(),
                entity.getUserId(),
                entity.getContent(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
