package com.linkvault.linkvaultserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("lv_sms_code")
public class SmsCodeEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String phone;

    private String code;

    private String scene;

    private LocalDateTime expiresAt;

    private LocalDateTime usedAt;

    private Integer failCount;

    private String sendIp;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
