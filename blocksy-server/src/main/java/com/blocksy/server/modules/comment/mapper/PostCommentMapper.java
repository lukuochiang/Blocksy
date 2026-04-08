package com.blocksy.server.modules.comment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blocksy.server.modules.comment.entity.PostCommentEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PostCommentMapper extends BaseMapper<PostCommentEntity> {
}
