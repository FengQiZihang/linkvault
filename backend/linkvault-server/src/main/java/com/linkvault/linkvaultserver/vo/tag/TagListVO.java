package com.linkvault.linkvaultserver.vo.tag;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagListVO {

    private List<TagVO> pinned; // 置顶标签列表

    private List<TagVO> normal; // 普通标签列表
}
