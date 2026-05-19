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
@TableName("lv_sms_code")
public class SmsCodeEntity {

    @TableId(type = IdType.AUTO)
    private Long id; // 验证码记录ID，数据库自增主键

    private String phone; // 接收验证码的手机号

    private String code; // 验证码内容，MVP开发阶段使用固定验证码

    private String scene; // 验证码使用场景，例如LOGIN

    private LocalDateTime expiresAt; // 验证码过期时间

    private LocalDateTime usedAt; // 验证码使用时间，未使用时为null

    private Integer failCount; // 当前验证码校验失败次数

    private String sendIp; // 发送验证码请求IP，当前阶段可为空

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt; // 创建时间

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt; // 更新时间
}
