package com.linkvault.linkvaultserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("lv_user")
public class UserEntity {

    @TableId(type = IdType.AUTO)
    private Long id; // 用户ID，数据库自增主键

    private String phone; // 手机号，登录唯一标识

    private String nickname; // 用户昵称

    private String avatarUrl; // 系统预置头像路径

    private String status; // 用户状态，ACTIVE/DISABLED

    private LocalDateTime lastLoginAt; // 最近登录时间

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt; // 创建时间

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt; // 更新时间
}
