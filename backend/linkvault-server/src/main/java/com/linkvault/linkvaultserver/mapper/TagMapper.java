package com.linkvault.linkvaultserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.linkvault.linkvaultserver.entity.TagEntity;
import com.linkvault.linkvaultserver.vo.tag.TagVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface TagMapper extends BaseMapper<TagEntity> {

    List<TagVO> selectTagListByUserId(@Param("userId") Long userId);

    TagVO selectTagVOByUserIdAndTagId(@Param("userId") Long userId, @Param("tagId") Long tagId);

    int updateTagNameByUserId(@Param("userId") Long userId,
                              @Param("tagId") Long tagId,
                              @Param("name") String name,
                              @Param("updatedAt") LocalDateTime updatedAt);

    int updateTagPinnedByUserId(@Param("userId") Long userId,
                                @Param("tagId") Long tagId,
                                @Param("pinned") Boolean pinned,
                                @Param("updatedAt") LocalDateTime updatedAt);

    Long countBookmarkTagByUserIdAndTagId(@Param("userId") Long userId, @Param("tagId") Long tagId);

    int insertMergedBookmarkTags(@Param("userId") Long userId,
                                 @Param("sourceTagId") Long sourceTagId,
                                 @Param("targetTagId") Long targetTagId,
                                 @Param("now") LocalDateTime now);

    int deleteBookmarkTagsByUserIdAndTagId(@Param("userId") Long userId, @Param("tagId") Long tagId);

    int deleteTagByUserId(@Param("userId") Long userId, @Param("tagId") Long tagId);
}
