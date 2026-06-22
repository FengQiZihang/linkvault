package com.linkvault.linkvaultserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.linkvault.linkvaultserver.entity.LinkEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LinkMapper extends BaseMapper<LinkEntity> {
}
