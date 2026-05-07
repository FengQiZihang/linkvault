package com.linkvault.linkvaultserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.linkvault.linkvaultserver.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
}
