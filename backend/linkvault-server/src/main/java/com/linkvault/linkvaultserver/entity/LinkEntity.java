package com.linkvault.linkvaultserver.entity;

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
@TableName("lv_link")
public class LinkEntity {

    @TableId
    private Long id; // 链接ID

    private String originalUrl; // 首次入库时的原始URL，仅用于排查

    private String normalizedUrl; // 用于链接对象去重的规范化URL

    private String finalUrl; // 短链展开后的最终URL

    private String urlHash; // normalizedUrl的SHA-256哈希

    private String platform; // 信息源平台

    private String publisher; // 发布作者

    private String title; // 标题

    private LocalDateTime publishedAt; // 内容发布时间

    private String metaStatus; // 元信息抓取状态

    private LocalDateTime metaFetchedAt; // 最近一次元信息抓取完成时间

    private String metaError; // 最近一次抓取失败原因摘要

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt; // 创建时间

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt; // 更新时间
}
