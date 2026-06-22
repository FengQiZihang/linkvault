package com.linkvault.linkvaultserver.component.metadata;

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
public class UrlMetadataResult {

    private String finalUrl; // 短链展开或平台规范化后的最终URL

    private String platform; // 平台枚举值

    private String publisher; // 发布作者

    private String title; // 标题

    private LocalDateTime publishedAt; // 内容发布时间，按UTC解释

    private String metaStatus; // 元信息状态：SUCCESS/PARTIAL/FAILED

    private String metaError; // 抓取失败或部分成功原因
}
