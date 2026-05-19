package com.linkvault.linkvaultserver.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.linkvault.linkvaultserver.common.TimeUtils;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MybatisPlusMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        // 1、生成本次插入使用的UTC时间
        LocalDateTime now = TimeUtils.nowUtc();

        // 2、插入时同时填充createdAt和updatedAt
        strictInsertFill(metaObject, "createdAt", LocalDateTime.class, now);
        strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, now);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 更新时只刷新updatedAt，createdAt保持原始创建时间
        setFieldValByName("updatedAt", TimeUtils.nowUtc(), metaObject);
    }
}
