package com.linkvault.linkvaultserver.service;

import com.linkvault.linkvaultserver.vo.tag.TagListVO;
import com.linkvault.linkvaultserver.vo.tag.TagVO;

public interface TagService {

    TagListVO listTags();

    TagVO createTag(String name, Boolean pinned);

    TagVO renameTag(Long tagId, String name);

    TagVO updateTagPinned(Long tagId, Boolean pinned);

    Boolean deleteTag(Long tagId);

    Boolean mergeTag(Long sourceTagId, Long targetTagId);
}
