package com.linkvault.linkvaultserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("lv_user")
public class UserEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String phone;

    private String nickname;

    private String avatarUrl;

    private String status;

    private LocalDateTime lastLoginAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
