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
@TableName("lv_bookmark")
public class BookmarkEntity {

    @TableId
    private Long id; // 收藏ID

    private Long userId; // 所属用户ID

    private Long linkId; // 链接ID

    private String originalUrl; // 当前用户本次粘贴的原始URL

    private String note; // 用户备注，未填写时为空字符串

    private LocalDateTime savedAt; // 用户保存时间

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt; // 创建时间

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt; // 更新时间
}
