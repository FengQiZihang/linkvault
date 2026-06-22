package com.linkvault.linkvaultserver.vo.bookmark;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkMetaVO {

    private String originalUrl; // 当前收藏记录保存的用户原始输入URL

    private String platform; // 信息源平台

    private String publisher; // 发布作者

    private String title; // 标题

    private String publishedAt; // 内容发布时间，UTC ISO 8601字符串

    private String metaStatus; // 元信息抓取状态
}
