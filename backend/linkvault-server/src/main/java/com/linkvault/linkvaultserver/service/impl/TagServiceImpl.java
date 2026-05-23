package com.linkvault.linkvaultserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.linkvault.linkvaultserver.common.TimeUtils;
import com.linkvault.linkvaultserver.context.CurrentUserInfo;
import com.linkvault.linkvaultserver.context.UserContext;
import com.linkvault.linkvaultserver.entity.TagEntity;
import com.linkvault.linkvaultserver.exception.BusinessException;
import com.linkvault.linkvaultserver.exception.ErrorCode;
import com.linkvault.linkvaultserver.mapper.TagMapper;
import com.linkvault.linkvaultserver.service.TagService;
import com.linkvault.linkvaultserver.vo.tag.TagListVO;
import com.linkvault.linkvaultserver.vo.tag.TagVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private static final int MAX_TAG_NAME_LENGTH = 20; // 标签名称最大字符数

    private final TagMapper tagMapper; // 标签表数据访问对象

    @Override
    public TagListVO listTags() {
        // 1、获取当前登录用户
        CurrentUserInfo currentUser = getRequiredCurrentUser();

        // 2、查询当前用户标签及关联收藏数量
        List<TagVO> tags = tagMapper.selectTagListByUserId(currentUser.getUserId());

        // 3、按置顶状态分组返回
        List<TagVO> pinnedTags = tags.stream()
                .filter(tag -> Boolean.TRUE.equals(tag.getPinned()))
                .toList();
        List<TagVO> normalTags = tags.stream()
                .filter(tag -> !Boolean.TRUE.equals(tag.getPinned()))
                .toList();

        log.info("获取标签列表完成，userId={}, pinnedCount={}, normalCount={}",
                currentUser.getUserId(), pinnedTags.size(), normalTags.size());
        return TagListVO.builder()
                .pinned(pinnedTags)
                .normal(normalTags)
                .build();
    }

    @Transactional
    @Override
    public TagVO createTag(String name, Boolean pinned) {
        // 1、获取当前登录用户并清洗标签名称
        CurrentUserInfo currentUser = getRequiredCurrentUser();
        String normalizedName = normalizeTagName(name);

        // 2、校验当前用户下标签名是否重复
        checkTagNameNotExists(currentUser.getUserId(), normalizedName);

        // 3、写入标签记录
        TagEntity tagEntity = TagEntity.builder()
                .userId(currentUser.getUserId())
                .name(normalizedName)
                .pinned(normalizePinned(pinned))
                .build();
        insertTag(tagEntity);

        // 4、转换为前端需要的标签VO
        log.info("创建标签完成，userId={}, tagId={}, pinned={}",
                currentUser.getUserId(), tagEntity.getId(), tagEntity.getPinned());
        return toTagVO(tagEntity);
    }

    @Transactional
    @Override
    public TagVO renameTag(Long tagId, String name) {
        // 1、获取当前登录用户并校验标签归属
        CurrentUserInfo currentUser = getRequiredCurrentUser();
        Long normalizedTagId = normalizeTagId(tagId);
        TagEntity tagEntity = getRequiredTag(currentUser.getUserId(), normalizedTagId);
        String normalizedName = normalizeTagName(name);

        // 2、校验新名称在当前用户下不能与其他标签重复
        if (!tagEntity.getName().equals(normalizedName)) {
            checkTagNameNotExists(currentUser.getUserId(), normalizedName, normalizedTagId);
            updateTagName(currentUser.getUserId(), normalizedTagId, normalizedName);
        }

        // 3、返回带当前关联收藏数量的标签对象
        log.info("重命名标签完成，userId={}, tagId={}, name={}",
                currentUser.getUserId(), normalizedTagId, normalizedName);
        return getRequiredTagVO(currentUser.getUserId(), normalizedTagId);
    }

    @Transactional
    @Override
    public TagVO updateTagPinned(Long tagId, Boolean pinned) {
        // 1、获取当前登录用户并校验标签归属
        CurrentUserInfo currentUser = getRequiredCurrentUser();
        Long normalizedTagId = normalizeTagId(tagId);
        getRequiredTag(currentUser.getUserId(), normalizedTagId);
        Boolean normalizedPinned = normalizePinned(pinned);

        // 2、更新当前用户标签的置顶状态
        int updated = tagMapper.updateTagPinnedByUserId(
                currentUser.getUserId(), normalizedTagId, normalizedPinned, TimeUtils.nowUtc());
        if (updated <= 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        // 3、返回带当前关联收藏数量的标签对象
        log.info("更新标签置顶状态完成，userId={}, tagId={}, pinned={}",
                currentUser.getUserId(), normalizedTagId, normalizedPinned);
        return getRequiredTagVO(currentUser.getUserId(), normalizedTagId);
    }

    @Transactional
    @Override
    public Boolean deleteTag(Long tagId) {
        // 1、获取当前登录用户并校验标签归属
        CurrentUserInfo currentUser = getRequiredCurrentUser();
        Long normalizedTagId = normalizeTagId(tagId);
        getRequiredTag(currentUser.getUserId(), normalizedTagId);

        // 2、有收藏关联的标签不允许直接删除
        Long bookmarkCount = tagMapper.countBookmarkTagByUserIdAndTagId(currentUser.getUserId(), normalizedTagId);
        if (bookmarkCount != null && bookmarkCount > 0) {
            throw new BusinessException(ErrorCode.CONFLICT);
        }

        // 3、物理删除当前用户标签
        int deleted = tagMapper.deleteTagByUserId(currentUser.getUserId(), normalizedTagId);
        if (deleted <= 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        log.info("删除标签完成，userId={}, tagId={}", currentUser.getUserId(), normalizedTagId);
        return true;
    }

    @Transactional
    @Override
    public Boolean mergeTag(Long sourceTagId, Long targetTagId) {
        // 1、获取当前登录用户并校验源标签和目标标签
        CurrentUserInfo currentUser = getRequiredCurrentUser();
        Long normalizedSourceTagId = normalizeTagId(sourceTagId);
        Long normalizedTargetTagId = normalizeTagId(targetTagId);
        if (normalizedSourceTagId.equals(normalizedTargetTagId)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        getRequiredTag(currentUser.getUserId(), normalizedSourceTagId);
        getRequiredTag(currentUser.getUserId(), normalizedTargetTagId);

        // 2、把源标签关系迁移到目标标签；同一收藏已有关联目标标签时由唯一索引去重
        tagMapper.insertMergedBookmarkTags(
                currentUser.getUserId(), normalizedSourceTagId, normalizedTargetTagId, TimeUtils.nowUtc());

        // 3、删除源标签关系和源标签本身
        tagMapper.deleteBookmarkTagsByUserIdAndTagId(currentUser.getUserId(), normalizedSourceTagId);
        int deleted = tagMapper.deleteTagByUserId(currentUser.getUserId(), normalizedSourceTagId);
        if (deleted <= 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        log.info("合并标签完成，userId={}, sourceTagId={}, targetTagId={}",
                currentUser.getUserId(), normalizedSourceTagId, normalizedTargetTagId);
        return true;
    }

    private CurrentUserInfo getRequiredCurrentUser() {
        CurrentUserInfo currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return currentUser;
    }

    private String normalizeTagName(String name) {
        String normalizedName = name == null ? "" : name.trim();
        if (normalizedName.isEmpty() || normalizedName.length() > MAX_TAG_NAME_LENGTH) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        return normalizedName;
    }

    private Long normalizeTagId(Long tagId) {
        if (tagId == null || tagId <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        return tagId;
    }

    private Boolean normalizePinned(Boolean pinned) {
        if (pinned == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        return pinned;
    }

    private void checkTagNameNotExists(Long userId, String name) {
        Long count = tagMapper.selectCount(
                new LambdaQueryWrapper<TagEntity>()
                        .eq(TagEntity::getUserId, userId)
                        .eq(TagEntity::getName, name)
        );
        if (count != null && count > 0) {
            throw new BusinessException(ErrorCode.CONFLICT);
        }
    }

    private void checkTagNameNotExists(Long userId, String name, Long excludedTagId) {
        Long count = tagMapper.selectCount(
                new LambdaQueryWrapper<TagEntity>()
                        .eq(TagEntity::getUserId, userId)
                        .eq(TagEntity::getName, name)
                        .ne(TagEntity::getId, excludedTagId)
        );
        if (count != null && count > 0) {
            throw new BusinessException(ErrorCode.CONFLICT);
        }
    }

    private TagEntity getRequiredTag(Long userId, Long tagId) {
        TagEntity tagEntity = tagMapper.selectOne(
                new LambdaQueryWrapper<TagEntity>()
                        .eq(TagEntity::getUserId, userId)
                        .eq(TagEntity::getId, tagId)
                        .last("limit 1")
        );
        if (tagEntity == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        return tagEntity;
    }

    private TagVO getRequiredTagVO(Long userId, Long tagId) {
        TagVO tagVO = tagMapper.selectTagVOByUserIdAndTagId(userId, tagId);
        if (tagVO == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        return tagVO;
    }

    private void updateTagName(Long userId, Long tagId, String name) {
        try {
            int updated = tagMapper.updateTagNameByUserId(userId, tagId, name, TimeUtils.nowUtc());
            if (updated <= 0) {
                throw new BusinessException(ErrorCode.NOT_FOUND);
            }
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ErrorCode.CONFLICT);
        }
    }

    private void insertTag(TagEntity tagEntity) {
        try {
            tagMapper.insert(tagEntity);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ErrorCode.CONFLICT);
        }
    }

    private TagVO toTagVO(TagEntity tagEntity) {
        return TagVO.builder()
                .tagId(tagEntity.getId())
                .name(tagEntity.getName())
                .pinned(Boolean.TRUE.equals(tagEntity.getPinned()))
                .bookmarkCount(0L)
                .build();
    }
}
