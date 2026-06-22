package com.linkvault.linkvaultserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkvault.linkvaultserver.common.PageResponse;
import com.linkvault.linkvaultserver.common.TimeUtils;
import com.linkvault.linkvaultserver.component.metadata.UrlMetadataFetcher;
import com.linkvault.linkvaultserver.component.metadata.UrlMetadataResult;
import com.linkvault.linkvaultserver.context.CurrentUserInfo;
import com.linkvault.linkvaultserver.context.UserContext;
import com.linkvault.linkvaultserver.entity.BookmarkEntity;
import com.linkvault.linkvaultserver.entity.BookmarkTagEntity;
import com.linkvault.linkvaultserver.entity.LinkEntity;
import com.linkvault.linkvaultserver.entity.TagEntity;
import com.linkvault.linkvaultserver.exception.BusinessException;
import com.linkvault.linkvaultserver.exception.ErrorCode;
import com.linkvault.linkvaultserver.mapper.BookmarkMapper;
import com.linkvault.linkvaultserver.mapper.BookmarkTagMapper;
import com.linkvault.linkvaultserver.mapper.LinkMapper;
import com.linkvault.linkvaultserver.mapper.TagMapper;
import com.linkvault.linkvaultserver.service.BookmarkService;
import com.linkvault.linkvaultserver.vo.bookmark.BookmarkDetailVO;
import com.linkvault.linkvaultserver.vo.bookmark.BookmarkListItemVO;
import com.linkvault.linkvaultserver.vo.bookmark.BookmarkNoteVO;
import com.linkvault.linkvaultserver.vo.bookmark.ImportBookmarkVO;
import com.linkvault.linkvaultserver.vo.bookmark.LinkMetaVO;
import com.linkvault.linkvaultserver.vo.bookmark.TagSimpleVO;
import com.linkvault.linkvaultserver.vo.bookmark.UpdateBookmarkTagsVO;
import com.linkvault.linkvaultserver.vo.bookmark.OrganizeBookmarkVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookmarkServiceImpl implements BookmarkService {

    private static final int MAX_URL_LENGTH = 2048; // URL最大长度
    private static final int MAX_NOTE_LENGTH = 1000; // 备注最大字符数
    private static final int DEFAULT_RECENT_PAGE_SIZE = 5; // 首页最近保存默认条数
    private static final String DEFAULT_PUBLISHER = "未知作者"; // 元信息未抓取前的默认作者
    private static final String DEFAULT_TITLE = "未解析标题"; // 元信息未抓取前的默认标题
    private static final String DEFAULT_PLATFORM = "UNKNOWN"; // 默认平台
    private static final String DEFAULT_META_STATUS = "PENDING"; // 新链接默认元信息状态
    private static final Set<String> TRACKING_QUERY_KEYS = Set.of(
            "utm_source", "utm_medium", "utm_campaign", "utm_term", "utm_content", "si", "fbclid", "gclid"
    );

    private final LinkMapper linkMapper; // 链接对象数据访问对象
    private final BookmarkMapper bookmarkMapper; // 收藏数据访问对象
    private final BookmarkTagMapper bookmarkTagMapper; // 收藏标签关系数据访问对象
    private final TagMapper tagMapper; // 标签数据访问对象
    private final UrlMetadataFetcher urlMetadataFetcher; // 链接元信息抓取组件

    @Transactional
    @Override
    public ImportBookmarkVO importBookmark(String url) {
        // 1、获取当前登录用户并校验URL
        CurrentUserInfo currentUser = getRequiredCurrentUser();
        String originalUrl = normalizeOriginalUrl(url);
        String normalizedUrl = normalizeUrlForDedup(originalUrl);
        String urlHash = sha256(normalizedUrl);

        // 2、查询或创建链接对象
        LinkEntity linkEntity = getOrCreateLink(originalUrl, normalizedUrl, urlHash);
        refreshMetadataIfNecessary(linkEntity, originalUrl);

        // 3、查询或创建当前用户收藏记录
        BookmarkEntity bookmarkEntity = findBookmarkByUserIdAndLinkId(currentUser.getUserId(), linkEntity.getId());
        boolean duplicated = bookmarkEntity != null;
        if (bookmarkEntity == null) {
            bookmarkEntity = createBookmark(currentUser.getUserId(), linkEntity.getId(), originalUrl);
        }

        log.info("导入收藏完成，userId={}, bookmarkId={}, linkId={}, duplicated={}",
                currentUser.getUserId(), bookmarkEntity.getId(), linkEntity.getId(), duplicated);
        return buildImportBookmarkVO(bookmarkEntity, linkEntity, duplicated);
    }

    @Override
    public PageResponse<BookmarkListItemVO> listRecentBookmarks(Integer page, Integer pageSize) {
        // 1、获取当前登录用户并规范化分页参数
        CurrentUserInfo currentUser = getRequiredCurrentUser();
        Integer normalizedPage = normalizePage(page);
        Integer normalizedPageSize = normalizePageSize(pageSize, DEFAULT_RECENT_PAGE_SIZE);

        // 2、使用MyBatis-Plus分页插件查询当前用户最近保存收藏
        Page<BookmarkEntity> bookmarkPage = bookmarkMapper.selectPage(
                Page.of(normalizedPage, normalizedPageSize),
                new LambdaQueryWrapper<BookmarkEntity>()
                        .eq(BookmarkEntity::getUserId, currentUser.getUserId())
                        .orderByDesc(BookmarkEntity::getSavedAt)
                        .orderByDesc(BookmarkEntity::getId)
        );
        List<BookmarkEntity> bookmarks = bookmarkPage.getRecords();

        // 3、批量查询链接和标签并组装分页VO
        Map<Long, LinkEntity> linkMap = listLinksByBookmarks(bookmarks);
        Map<Long, List<TagSimpleVO>> tagMap = listTagsByBookmarkIds(
                currentUser.getUserId(), bookmarks.stream().map(BookmarkEntity::getId).toList());
        List<BookmarkListItemVO> items = bookmarks.stream()
                .map(bookmark -> toBookmarkListItemVO(
                        bookmark,
                        getRequiredLink(linkMap, bookmark.getLinkId()),
                        tagMap.getOrDefault(bookmark.getId(), Collections.emptyList())))
                .toList();

        log.info("获取最近保存收藏完成，userId={}, page={}, pageSize={}, total={}",
                currentUser.getUserId(), normalizedPage, normalizedPageSize, bookmarkPage.getTotal());
        return PageResponse.of(items, normalizedPage, normalizedPageSize, bookmarkPage.getTotal());
    }

    @Override
    public BookmarkDetailVO getBookmarkDetail(Long bookmarkId) {
        // 1、获取当前登录用户并校验收藏ID
        CurrentUserInfo currentUser = getRequiredCurrentUser();
        Long normalizedBookmarkId = normalizeBookmarkId(bookmarkId);

        // 2、按当前用户和收藏ID查询收藏与链接对象
        BookmarkEntity bookmarkEntity = findBookmarkByUserIdAndBookmarkId(currentUser.getUserId(), normalizedBookmarkId);
        if (bookmarkEntity == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        LinkEntity linkEntity = linkMapper.selectById(bookmarkEntity.getLinkId());
        if (linkEntity == null) {
            throw new BusinessException(ErrorCode.SERVER_ERROR);
        }

        // 3、查询当前收藏标签并返回详情VO
        List<TagSimpleVO> tags = listTagsByBookmarkIds(
                currentUser.getUserId(), List.of(normalizedBookmarkId))
                .getOrDefault(normalizedBookmarkId, Collections.emptyList());
        log.info("获取收藏详情完成，userId={}, bookmarkId={}", currentUser.getUserId(), normalizedBookmarkId);
        return toBookmarkDetailVO(bookmarkEntity, linkEntity, tags);
    }

    @Transactional
    @Override
    public BookmarkNoteVO updateBookmarkNote(Long bookmarkId, String note) {
        // 1、获取当前登录用户并校验入参
        CurrentUserInfo currentUser = getRequiredCurrentUser();
        Long normalizedBookmarkId = normalizeBookmarkId(bookmarkId);
        String normalizedNote = normalizeNote(note);

        // 2、按当前用户和收藏ID更新备注
        int updated = bookmarkMapper.update(
                null,
                new LambdaUpdateWrapper<BookmarkEntity>()
                        .eq(BookmarkEntity::getUserId, currentUser.getUserId())
                        .eq(BookmarkEntity::getId, normalizedBookmarkId)
                        .set(BookmarkEntity::getNote, normalizedNote)
                        .set(BookmarkEntity::getUpdatedAt, TimeUtils.nowUtc())
        );
        if (updated <= 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        // 3、返回更新后的备注对象
        log.info("更新收藏备注完成，userId={}, bookmarkId={}", currentUser.getUserId(), normalizedBookmarkId);
        return BookmarkNoteVO.builder()
                .bookmarkId(normalizedBookmarkId)
                .note(normalizedNote)
                .build();
    }

    @Transactional
    @Override
    public Boolean deleteBookmark(Long bookmarkId) {
        // 1、获取当前登录用户并校验收藏ID
        CurrentUserInfo currentUser = getRequiredCurrentUser();
        Long normalizedBookmarkId = normalizeBookmarkId(bookmarkId);

        // 2、删除当前用户该收藏的标签关系
        bookmarkTagMapper.delete(
                new LambdaQueryWrapper<BookmarkTagEntity>()
                        .eq(BookmarkTagEntity::getUserId, currentUser.getUserId())
                        .eq(BookmarkTagEntity::getBookmarkId, normalizedBookmarkId)
        );

        // 3、物理删除当前用户收藏，不删除链接对象
        int deleted = bookmarkMapper.delete(
                new LambdaQueryWrapper<BookmarkEntity>()
                        .eq(BookmarkEntity::getUserId, currentUser.getUserId())
                        .eq(BookmarkEntity::getId, normalizedBookmarkId)
        );
        if (deleted <= 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        log.info("删除收藏完成，userId={}, bookmarkId={}", currentUser.getUserId(), normalizedBookmarkId);
        return true;
    }

    private CurrentUserInfo getRequiredCurrentUser() {
        CurrentUserInfo currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return currentUser;
    }

    private String normalizeOriginalUrl(String url) {
        String normalizedUrl = url == null ? "" : url.trim();
        if (normalizedUrl.isEmpty() || normalizedUrl.length() > MAX_URL_LENGTH) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        try {
            URI uri = new URI(normalizedUrl);
            String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
            if (!"http".equals(scheme) && !"https".equals(scheme)) {
                throw new BusinessException(ErrorCode.PARAM_ERROR);
            }
            if (uri.getHost() == null || uri.getHost().isBlank()) {
                throw new BusinessException(ErrorCode.PARAM_ERROR);
            }
            return normalizedUrl;
        } catch (URISyntaxException e) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
    }

    private String normalizeUrlForDedup(String originalUrl) {
        try {
            URI uri = new URI(originalUrl);
            String scheme = uri.getScheme().toLowerCase(Locale.ROOT);
            String host = uri.getHost().toLowerCase(Locale.ROOT);
            String path = normalizePath(uri.getRawPath());

            if ("youtu.be".equals(host)) {
                String videoId = path.startsWith("/") ? path.substring(1) : path;
                return "https://www.youtube.com/watch?v=" + videoId;
            }

            if (host.endsWith("youtube.com") && "/watch".equals(path)) {
                String videoId = getQueryValue(uri.getRawQuery(), "v");
                if (videoId != null && !videoId.isBlank()) {
                    return "https://www.youtube.com/watch?v=" + videoId;
                }
            }

            if ("x.com".equals(host) || "www.x.com".equals(host)) {
                host = "twitter.com";
            } else if (host.startsWith("www.")) {
                host = host.substring(4);
            }

            String query = normalizeQuery(uri.getRawQuery());
            return scheme + "://" + host + path + (query.isEmpty() ? "" : "?" + query);
        } catch (URISyntaxException e) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
    }

    private String normalizePath(String rawPath) {
        if (rawPath == null || rawPath.isBlank()) {
            return "";
        }
        if (rawPath.length() > 1 && rawPath.endsWith("/")) {
            return rawPath.substring(0, rawPath.length() - 1);
        }
        return rawPath;
    }

    private String normalizeQuery(String rawQuery) {
        if (rawQuery == null || rawQuery.isBlank()) {
            return "";
        }
        List<String> keptPairs = new ArrayList<>();
        for (String pair : rawQuery.split("&")) {
            String key = pair.contains("=") ? pair.substring(0, pair.indexOf('=')) : pair;
            if (!TRACKING_QUERY_KEYS.contains(key.toLowerCase(Locale.ROOT))) {
                keptPairs.add(pair);
            }
        }
        Collections.sort(keptPairs);
        return String.join("&", keptPairs);
    }

    private String getQueryValue(String rawQuery, String expectedKey) {
        if (rawQuery == null || rawQuery.isBlank()) {
            return null;
        }
        for (String pair : rawQuery.split("&")) {
            int index = pair.indexOf('=');
            String key = index >= 0 ? pair.substring(0, index) : pair;
            String value = index >= 0 ? pair.substring(index + 1) : "";
            if (expectedKey.equals(key)) {
                return value;
            }
        }
        return null;
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : bytes) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new BusinessException(ErrorCode.SERVER_ERROR);
        }
    }

    private LinkEntity getOrCreateLink(String originalUrl, String normalizedUrl, String urlHash) {
        LinkEntity existingLink = findLinkByHashOrOriginalUrl(urlHash, originalUrl);
        if (existingLink != null) {
            return existingLink;
        }

        LinkEntity linkEntity = LinkEntity.builder()
                .originalUrl(originalUrl)
                .normalizedUrl(normalizedUrl)
                .urlHash(urlHash)
                .platform(detectPlatform(normalizedUrl))
                .publisher(DEFAULT_PUBLISHER)
                .title(DEFAULT_TITLE)
                .metaStatus(DEFAULT_META_STATUS)
                .build();
        try {
            linkMapper.insert(linkEntity);
            return linkEntity;
        } catch (DuplicateKeyException e) {
            return findRequiredLinkByHash(urlHash);
        }
    }

    private void refreshMetadataIfNecessary(LinkEntity linkEntity, String originalUrl) {
        if (!needRefreshMetadata(linkEntity)) {
            return;
        }

        UrlMetadataResult metadata = urlMetadataFetcher.fetch(originalUrl);
        linkEntity.setFinalUrl(metadata.getFinalUrl());
        linkEntity.setPlatform(choosePlatform(metadata.getPlatform(), linkEntity.getPlatform()));
        linkEntity.setPublisher(firstNonBlank(metadata.getPublisher(), DEFAULT_PUBLISHER));
        linkEntity.setTitle(firstNonBlank(metadata.getTitle(), DEFAULT_TITLE));
        linkEntity.setPublishedAt(metadata.getPublishedAt());
        linkEntity.setMetaStatus(firstNonBlank(metadata.getMetaStatus(), DEFAULT_META_STATUS));
        linkEntity.setMetaFetchedAt(TimeUtils.nowUtc());
        linkEntity.setMetaError(metadata.getMetaError());
        linkMapper.updateById(linkEntity);
    }

    private boolean needRefreshMetadata(LinkEntity linkEntity) {
        if (linkEntity.getMetaFetchedAt() == null) {
            return true;
        }
        if ("PENDING".equals(linkEntity.getMetaStatus()) || "FAILED".equals(linkEntity.getMetaStatus())) {
            return true;
        }
        if ("SUCCESS".equals(linkEntity.getMetaStatus())
                && linkEntity.getMetaError() != null
                && !linkEntity.getMetaError().isBlank()) {
            return true;
        }
        return DEFAULT_TITLE.equals(linkEntity.getTitle()) || DEFAULT_PUBLISHER.equals(linkEntity.getPublisher());
    }

    private String choosePlatform(String fetchedPlatform, String currentPlatform) {
        if (fetchedPlatform != null && !fetchedPlatform.isBlank() && !DEFAULT_PLATFORM.equals(fetchedPlatform)) {
            return fetchedPlatform;
        }
        return firstNonBlank(currentPlatform, DEFAULT_PLATFORM);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private LinkEntity findLinkByHashOrOriginalUrl(String urlHash, String originalUrl) {
        return linkMapper.selectOne(
                new LambdaQueryWrapper<LinkEntity>()
                        .eq(LinkEntity::getUrlHash, urlHash)
                        .or()
                        .eq(LinkEntity::getOriginalUrl, originalUrl)
                        .orderByAsc(LinkEntity::getId)
                        .last("limit 1")
        );
    }

    private LinkEntity findRequiredLinkByHash(String urlHash) {
        LinkEntity linkEntity = linkMapper.selectOne(
                new LambdaQueryWrapper<LinkEntity>()
                        .eq(LinkEntity::getUrlHash, urlHash)
                        .last("limit 1")
        );
        if (linkEntity == null) {
            throw new BusinessException(ErrorCode.SERVER_ERROR);
        }
        return linkEntity;
    }

    private String detectPlatform(String normalizedUrl) {
        try {
            String host = new URI(normalizedUrl).getHost();
            if (host == null) {
                return DEFAULT_PLATFORM;
            }
            String normalizedHost = host.toLowerCase(Locale.ROOT);
            if (normalizedHost.contains("bilibili.com") || normalizedHost.contains("b23.tv")) {
                return "BILIBILI";
            }
            if (normalizedHost.contains("youtube.com") || normalizedHost.contains("youtu.be")) {
                return "YOUTUBE";
            }
            if (normalizedHost.contains("twitter.com") || normalizedHost.contains("x.com")) {
                return "X";
            }
            if (normalizedHost.contains("mp.weixin.qq.com")) {
                return "WECHAT_OFFICIAL_ACCOUNT";
            }
            return "WEB";
        } catch (URISyntaxException e) {
            return DEFAULT_PLATFORM;
        }
    }

    private BookmarkEntity findBookmarkByUserIdAndLinkId(Long userId, Long linkId) {
        return bookmarkMapper.selectOne(
                new LambdaQueryWrapper<BookmarkEntity>()
                        .eq(BookmarkEntity::getUserId, userId)
                        .eq(BookmarkEntity::getLinkId, linkId)
                        .last("limit 1")
        );
    }

    private BookmarkEntity findBookmarkByUserIdAndBookmarkId(Long userId, Long bookmarkId) {
        return bookmarkMapper.selectOne(
                new LambdaQueryWrapper<BookmarkEntity>()
                        .eq(BookmarkEntity::getUserId, userId)
                        .eq(BookmarkEntity::getId, bookmarkId)
                        .last("limit 1")
        );
    }

    private BookmarkEntity createBookmark(Long userId, Long linkId, String originalUrl) {
        BookmarkEntity bookmarkEntity = BookmarkEntity.builder()
                .userId(userId)
                .linkId(linkId)
                .originalUrl(originalUrl)
                .note("")
                .savedAt(TimeUtils.nowUtc())
                .build();
        try {
            bookmarkMapper.insert(bookmarkEntity);
            return bookmarkEntity;
        } catch (DuplicateKeyException e) {
            BookmarkEntity existingBookmark = findBookmarkByUserIdAndLinkId(userId, linkId);
            if (existingBookmark == null) {
                throw new BusinessException(ErrorCode.SERVER_ERROR);
            }
            return existingBookmark;
        }
    }

    private Integer normalizePage(Integer page) {
        if (page == null) {
            return 1;
        }
        if (page <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        return page;
    }

    private Integer normalizePageSize(Integer pageSize, Integer defaultPageSize) {
        if (pageSize == null) {
            return defaultPageSize;
        }
        if (pageSize <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        return pageSize;
    }

    private Long normalizeBookmarkId(Long bookmarkId) {
        if (bookmarkId == null || bookmarkId <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        return bookmarkId;
    }

    private String normalizeNote(String note) {
        if (note == null || note.length() > MAX_NOTE_LENGTH) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        return note;
    }

    private Map<Long, LinkEntity> listLinksByBookmarks(List<BookmarkEntity> bookmarks) {
        if (bookmarks == null || bookmarks.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> linkIds = bookmarks.stream()
                .map(BookmarkEntity::getLinkId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (linkIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return linkMapper.selectBatchIds(linkIds).stream()
                .collect(Collectors.toMap(LinkEntity::getId, Function.identity(), (left, right) -> left));
    }

    private LinkEntity getRequiredLink(Map<Long, LinkEntity> linkMap, Long linkId) {
        LinkEntity linkEntity = linkMap.get(linkId);
        if (linkEntity == null) {
            throw new BusinessException(ErrorCode.SERVER_ERROR);
        }
        return linkEntity;
    }

    private Map<Long, List<TagSimpleVO>> listTagsByBookmarkIds(Long userId, List<Long> bookmarkIds) {
        if (bookmarkIds == null || bookmarkIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> normalizedBookmarkIds = bookmarkIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (normalizedBookmarkIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<BookmarkTagEntity> relations = bookmarkTagMapper.selectList(
                new LambdaQueryWrapper<BookmarkTagEntity>()
                        .eq(BookmarkTagEntity::getUserId, userId)
                        .in(BookmarkTagEntity::getBookmarkId, normalizedBookmarkIds)
        );
        if (relations.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> tagIds = relations.stream()
                .map(BookmarkTagEntity::getTagId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (tagIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, TagEntity> tagEntityMap = tagMapper.selectList(
                        new LambdaQueryWrapper<TagEntity>()
                                .eq(TagEntity::getUserId, userId)
                                .in(TagEntity::getId, tagIds)
                ).stream()
                .collect(Collectors.toMap(TagEntity::getId, Function.identity(), (left, right) -> left));

        Map<Long, List<TagEntity>> groupedTags = new LinkedHashMap<>();
        for (BookmarkTagEntity relation : relations) {
            TagEntity tagEntity = tagEntityMap.get(relation.getTagId());
            if (tagEntity != null) {
                groupedTags.computeIfAbsent(relation.getBookmarkId(), key -> new ArrayList<>())
                        .add(tagEntity);
            }
        }

        Map<Long, List<TagSimpleVO>> tagMap = new LinkedHashMap<>();
        for (Map.Entry<Long, List<TagEntity>> entry : groupedTags.entrySet()) {
            List<TagSimpleVO> tags = entry.getValue().stream()
                    .sorted(Comparator
                            .comparing((TagEntity tag) -> !Boolean.TRUE.equals(tag.getPinned()))
                            .thenComparing(TagEntity::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                            .thenComparing(TagEntity::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                    .map(this::toTagSimpleVO)
                    .toList();
            tagMap.put(entry.getKey(), tags);
        }
        return tagMap;
    }

    private ImportBookmarkVO buildImportBookmarkVO(BookmarkEntity bookmarkEntity, LinkEntity linkEntity, boolean duplicated) {
        return ImportBookmarkVO.builder()
                .bookmarkId(bookmarkEntity.getId())
                .duplicated(duplicated)
                .link(LinkMetaVO.builder()
                        .originalUrl(bookmarkEntity.getOriginalUrl())
                        .platform(linkEntity.getPlatform())
                        .publisher(linkEntity.getPublisher())
                        .title(linkEntity.getTitle())
                        .publishedAt(TimeUtils.formatUtc(linkEntity.getPublishedAt()))
                        .metaStatus(linkEntity.getMetaStatus())
                        .build())
                .savedAt(TimeUtils.formatUtc(bookmarkEntity.getSavedAt()))
                .build();
    }

    private BookmarkListItemVO toBookmarkListItemVO(BookmarkEntity bookmarkEntity,
                                                    LinkEntity linkEntity,
                                                    List<TagSimpleVO> tags) {
        return BookmarkListItemVO.builder()
                .bookmarkId(bookmarkEntity.getId())
                .title(linkEntity.getTitle())
                .platform(linkEntity.getPlatform())
                .publisher(linkEntity.getPublisher())
                .publishedAt(TimeUtils.formatUtc(linkEntity.getPublishedAt()))
                .note(bookmarkEntity.getNote())
                .savedAt(TimeUtils.formatUtc(bookmarkEntity.getSavedAt()))
                .tags(tags)
                .build();
    }

    private BookmarkDetailVO toBookmarkDetailVO(BookmarkEntity bookmarkEntity,
                                                LinkEntity linkEntity,
                                                List<TagSimpleVO> tags) {
        return BookmarkDetailVO.builder()
                .bookmarkId(bookmarkEntity.getId())
                .link(LinkMetaVO.builder()
                        .originalUrl(bookmarkEntity.getOriginalUrl())
                        .platform(linkEntity.getPlatform())
                        .publisher(linkEntity.getPublisher())
                        .title(linkEntity.getTitle())
                        .publishedAt(TimeUtils.formatUtc(linkEntity.getPublishedAt()))
                        .metaStatus(linkEntity.getMetaStatus())
                        .build())
                .note(bookmarkEntity.getNote())
                .savedAt(TimeUtils.formatUtc(bookmarkEntity.getSavedAt()))
                .tags(tags == null ? Collections.emptyList() : tags)
                .build();
    }

    private TagSimpleVO toTagSimpleVO(TagEntity tagEntity) {
        return TagSimpleVO.builder()
                .tagId(tagEntity.getId())
                .name(tagEntity.getName())
                .pinned(Boolean.TRUE.equals(tagEntity.getPinned()))
                .build();
    }

    @Transactional
    @Override
    public UpdateBookmarkTagsVO updateBookmarkTags(Long bookmarkId, List<Long> tagIds) {
        CurrentUserInfo currentUser = getRequiredCurrentUser();
        Long normalizedBookmarkId = normalizeBookmarkId(bookmarkId);

        BookmarkEntity bookmark = findBookmarkByUserIdAndBookmarkId(currentUser.getUserId(), normalizedBookmarkId);
        if (bookmark == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        if (tagIds == null) {
            tagIds = Collections.emptyList();
        }
        
        List<Long> distinctTagIds = tagIds.stream().distinct().toList();
        if (!distinctTagIds.isEmpty()) {
            Long count = tagMapper.selectCount(
                    new LambdaQueryWrapper<TagEntity>()
                            .eq(TagEntity::getUserId, currentUser.getUserId())
                            .in(TagEntity::getId, distinctTagIds)
            );
            if (count != distinctTagIds.size()) {
                throw new BusinessException(ErrorCode.PARAM_ERROR);
            }
        }

        bookmarkTagMapper.delete(
                new LambdaQueryWrapper<BookmarkTagEntity>()
                        .eq(BookmarkTagEntity::getUserId, currentUser.getUserId())
                        .eq(BookmarkTagEntity::getBookmarkId, normalizedBookmarkId)
        );

        for (Long tagId : distinctTagIds) {
            BookmarkTagEntity relation = BookmarkTagEntity.builder()
                    .userId(currentUser.getUserId())
                    .bookmarkId(normalizedBookmarkId)
                    .tagId(tagId)
                    .build();
            bookmarkTagMapper.insert(relation);
        }

        List<TagSimpleVO> tags = listTagsByBookmarkIds(
                currentUser.getUserId(), List.of(normalizedBookmarkId))
                .getOrDefault(normalizedBookmarkId, Collections.emptyList());

        log.info("更新收藏标签完成，userId={}, bookmarkId={}, tagCount={}",
                currentUser.getUserId(), normalizedBookmarkId, tags.size());

        return UpdateBookmarkTagsVO.builder()
                .bookmarkId(normalizedBookmarkId)
                .tags(tags)
                .build();
    }

    @Transactional
    @Override
    public OrganizeBookmarkVO organizeBookmark(Long bookmarkId, String note, List<Long> tagIds) {
        CurrentUserInfo currentUser = getRequiredCurrentUser();
        Long normalizedBookmarkId = normalizeBookmarkId(bookmarkId);

        BookmarkEntity bookmark = findBookmarkByUserIdAndBookmarkId(currentUser.getUserId(), normalizedBookmarkId);
        if (bookmark == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        String normalizedNote = normalizeNote(note);
        bookmark.setNote(normalizedNote);
        bookmark.setUpdatedAt(TimeUtils.nowUtc());
        bookmarkMapper.updateById(bookmark);

        if (tagIds == null) {
            tagIds = Collections.emptyList();
        }
        
        List<Long> distinctTagIds = tagIds.stream().distinct().toList();
        if (!distinctTagIds.isEmpty()) {
            Long count = tagMapper.selectCount(
                    new LambdaQueryWrapper<TagEntity>()
                            .eq(TagEntity::getUserId, currentUser.getUserId())
                            .in(TagEntity::getId, distinctTagIds)
            );
            if (count != distinctTagIds.size()) {
                throw new BusinessException(ErrorCode.PARAM_ERROR);
            }
        }

        bookmarkTagMapper.delete(
                new LambdaQueryWrapper<BookmarkTagEntity>()
                        .eq(BookmarkTagEntity::getUserId, currentUser.getUserId())
                        .eq(BookmarkTagEntity::getBookmarkId, normalizedBookmarkId)
        );

        for (Long tagId : distinctTagIds) {
            BookmarkTagEntity relation = BookmarkTagEntity.builder()
                    .userId(currentUser.getUserId())
                    .bookmarkId(normalizedBookmarkId)
                    .tagId(tagId)
                    .build();
            bookmarkTagMapper.insert(relation);
        }

        List<TagSimpleVO> tags = listTagsByBookmarkIds(
                currentUser.getUserId(), List.of(normalizedBookmarkId))
                .getOrDefault(normalizedBookmarkId, Collections.emptyList());

        log.info("承接页整理收藏完成，userId={}, bookmarkId={}, noteLength={}, tagCount={}",
                currentUser.getUserId(), normalizedBookmarkId, normalizedNote.length(), tags.size());

        return OrganizeBookmarkVO.builder()
                .bookmarkId(normalizedBookmarkId)
                .note(normalizedNote)
                .tags(tags)
                .build();
    }

    @Override
    public PageResponse<BookmarkListItemVO> searchBookmarks(String keyword, List<Long> tagIds, Boolean untagged, Integer page, Integer pageSize) {
        CurrentUserInfo currentUser = getRequiredCurrentUser();
        Long userId = currentUser.getUserId();
        
        boolean isUntagged = Boolean.TRUE.equals(untagged);
        if (tagIds != null && !tagIds.isEmpty() && isUntagged) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        Integer normalizedPage = normalizePage(page);
        Integer normalizedPageSize = normalizePageSize(pageSize, 20);

        List<Long> matchedBookmarkIds = null;
        if (tagIds != null && !tagIds.isEmpty()) {
            List<Long> distinctTagIds = tagIds.stream().distinct().toList();
            List<BookmarkTagEntity> relations = bookmarkTagMapper.selectList(
                    new LambdaQueryWrapper<BookmarkTagEntity>()
                            .eq(BookmarkTagEntity::getUserId, userId)
                            .in(BookmarkTagEntity::getTagId, distinctTagIds)
            );
            
            Map<Long, Long> counts = relations.stream()
                    .collect(Collectors.groupingBy(BookmarkTagEntity::getBookmarkId, Collectors.counting()));
            
            matchedBookmarkIds = counts.entrySet().stream()
                    .filter(entry -> entry.getValue() == distinctTagIds.size())
                    .map(Map.Entry::getKey)
                    .toList();
            
            if (matchedBookmarkIds.isEmpty()) {
                return PageResponse.of(Collections.emptyList(), normalizedPage, normalizedPageSize, 0L);
            }
        }

        IPage<BookmarkEntity> bookmarkPage = bookmarkMapper.searchBookmarks(
                Page.of(normalizedPage, normalizedPageSize),
                userId,
                keyword != null ? keyword.trim() : null,
                matchedBookmarkIds,
                isUntagged
        );
        List<BookmarkEntity> bookmarks = bookmarkPage.getRecords();

        Map<Long, LinkEntity> linkMap = listLinksByBookmarks(bookmarks);
        Map<Long, List<TagSimpleVO>> tagMap = listTagsByBookmarkIds(
                userId, bookmarks.stream().map(BookmarkEntity::getId).toList());
        
        List<BookmarkListItemVO> items = bookmarks.stream()
                .map(bookmark -> toBookmarkListItemVO(
                        bookmark,
                        getRequiredLink(linkMap, bookmark.getLinkId()),
                        tagMap.getOrDefault(bookmark.getId(), Collections.emptyList())))
                .toList();

        log.info("搜索筛选收藏完成，userId={}, keyword={}, tagCount={}, untagged={}, page={}, pageSize={}, total={}",
                userId, keyword, tagIds != null ? tagIds.size() : 0, isUntagged, normalizedPage, normalizedPageSize, bookmarkPage.getTotal());

        return PageResponse.of(items, normalizedPage, normalizedPageSize, bookmarkPage.getTotal());
    }
}
