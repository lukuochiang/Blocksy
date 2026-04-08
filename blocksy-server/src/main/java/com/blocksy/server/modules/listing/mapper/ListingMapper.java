package com.blocksy.server.modules.listing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blocksy.server.modules.listing.entity.ListingEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ListingMapper extends BaseMapper<ListingEntity> {
}
