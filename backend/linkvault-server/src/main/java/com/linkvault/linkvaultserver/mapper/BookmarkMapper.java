package com.linkvault.linkvaultserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.linkvault.linkvaultserver.entity.BookmarkEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BookmarkMapper extends BaseMapper<BookmarkEntity> {

    @Select("<script>" +
            "SELECT b.* FROM lv_bookmark b " +
            "LEFT JOIN lv_link l ON b.link_id = l.id " +
            "WHERE b.user_id = #{userId} " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "  AND (l.title LIKE CONCAT('%', #{keyword}, '%') OR b.note LIKE CONCAT('%', #{keyword}, '%'))" +
            "</if>" +
            "<if test='bookmarkIds != null and bookmarkIds.size() > 0'>" +
            "  AND b.id IN " +
            "  <foreach item='id' collection='bookmarkIds' open='(' separator=',' close=')'>" +
            "    #{id}" +
            "  </foreach>" +
            "</if>" +
            "<if test='untagged == true'>" +
            "  AND NOT EXISTS (SELECT 1 FROM lv_bookmark_tag bt WHERE bt.bookmark_id = b.id AND bt.user_id = #{userId})" +
            "</if>" +
            "ORDER BY " +
            "<choose>" +
            "  <when test='keyword != null and keyword != \"\"'>" +
            "    CASE " +
            "      WHEN l.title = #{keyword} THEN 1 " +
            "      WHEN l.title LIKE CONCAT('%', #{keyword}, '%') THEN 2 " +
            "      ELSE 3 " +
            "    END ASC, b.saved_at DESC" +
            "  </when>" +
            "  <otherwise>b.saved_at DESC</otherwise>" +
            "</choose>" +
            "</script>")
    IPage<BookmarkEntity> searchBookmarks(IPage<BookmarkEntity> page,
                                          @Param("userId") Long userId,
                                          @Param("keyword") String keyword,
                                          @Param("bookmarkIds") List<Long> bookmarkIds,
                                          @Param("untagged") Boolean untagged);
}
