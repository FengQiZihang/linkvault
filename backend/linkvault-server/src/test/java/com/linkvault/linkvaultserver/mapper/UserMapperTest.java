package com.linkvault.linkvaultserver.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    void shouldQueryUserCount() {
        Long count = userMapper.selectCount(null);
        System.out.println("lv_user count = " + count);
    }
}
