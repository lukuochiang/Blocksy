package com.blocksy.server.modules.event.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blocksy.server.modules.event.entity.EventSignupEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EventSignupMapper extends BaseMapper<EventSignupEntity> {
}
