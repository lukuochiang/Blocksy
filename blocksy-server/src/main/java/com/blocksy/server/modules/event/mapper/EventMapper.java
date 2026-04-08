package com.blocksy.server.modules.event.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blocksy.server.modules.event.entity.EventEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EventMapper extends BaseMapper<EventEntity> {
}
