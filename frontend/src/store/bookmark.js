import { defineStore } from 'pinia';
import { api } from '@/api/client.js';

export const PLATFORM_MAP = {
  BILIBILI: { icon: '📺', label: 'Bilibili' },
  YOUTUBE: { icon: '▶️', label: 'YouTube' },
  X: { icon: '𝕏', label: 'X' },
  WECHAT_OFFICIAL_ACCOUNT: { icon: '💬', label: '微信公众号' },
  WEB: { icon: '🔗', label: '网页' },
  UNKNOWN: { icon: '🔗', label: '网页' },
  OTHER: { icon: '🔗', label: '网页' }
};

const normalizeTag = (tag) => {
  if (!tag) return null;
  return {
    tagId: tag.tagId,
    name: tag.name,
    pinned: !!tag.pinned,
    count: tag.bookmarkCount ?? tag.count ?? 0,
    bookmarkCount: tag.bookmarkCount ?? tag.count ?? 0
  };
};

const normalizeTags = (tags = []) => tags.map(normalizeTag).filter(Boolean);

const normalizeBookmark = (bookmark) => {
  if (!bookmark) return null;

  const tagObjects = normalizeTags(bookmark.tags || []);
  const link = bookmark.link || {};

  return {
    bookmarkId: bookmark.bookmarkId,
    title: bookmark.title || link.title || '未解析标题',
    platform: bookmark.platform || link.platform || 'UNKNOWN',
    publisher: bookmark.publisher || link.publisher || '未知作者',
    publishedAt: bookmark.publishedAt || link.publishedAt || null,
    note: bookmark.note || '',
    savedAt: bookmark.savedAt || '',
    originalUrl: bookmark.originalUrl || link.originalUrl || '',
    metaStatus: bookmark.metaStatus || link.metaStatus || '',
    tagObjects,
    tags: tagObjects.map(tag => tag.name)
  };
};

const findByName = (tags, name) => tags.find(tag => tag.name === name);

export const useBookmarkStore = defineStore('bookmark', {
  state: () => ({
    bookmarks: [],
    tags: [],
    stats: {
      totalBookmarkCount: 0,
      tagCount: 0,
      untaggedBookmarkCount: 0
    },
    page: 1,
    pageSize: 20,
    total: 0,
    loading: false
  }),

  getters: {
    sortedTags: (state) => [...state.tags].sort((a, b) => {
      if (a.pinned && !b.pinned) return -1;
      if (!a.pinned && b.pinned) return 1;
      return (b.count || 0) - (a.count || 0);
    }),
    untaggedBookmarks: (state) => state.bookmarks.filter(b => !b.tags || b.tags.length === 0)
  },

  actions: {
    setBookmarks(pageData) {
      this.bookmarks = (pageData?.items || []).map(normalizeBookmark).filter(Boolean);
      this.page = pageData?.page || 1;
      this.pageSize = pageData?.pageSize || 20;
      this.total = pageData?.total || this.bookmarks.length;
    },

    upsertBookmark(bookmark) {
      const normalized = normalizeBookmark(bookmark);
      if (!normalized) return null;

      const index = this.bookmarks.findIndex(item => item.bookmarkId === normalized.bookmarkId);
      if (index >= 0) {
        this.bookmarks.splice(index, 1, normalized);
      } else {
        this.bookmarks.unshift(normalized);
      }
      return normalized;
    },

    async fetchStats() {
      this.stats = await api.getStats();
      return this.stats;
    },

    async fetchTags() {
      const data = await api.listTags();
      this.tags = normalizeTags([...(data?.pinned || []), ...(data?.normal || [])]);
      return this.tags;
    },

    async fetchRecentBookmarks(page = 1, pageSize = 5) {
      const data = await api.listRecentBookmarks({ page, pageSize });
      this.setBookmarks(data);
      return this.bookmarks;
    },

    async fetchHomeData() {
      this.loading = true;
      try {
        await Promise.all([
          this.fetchStats(),
          this.fetchTags(),
          this.fetchRecentBookmarks(1, 5)
        ]);
      } finally {
        this.loading = false;
      }
    },

    async searchBookmarks({ keyword = '', tagNames = [], tagIds = [], untagged = false, page = 1, pageSize = 20 } = {}) {
      const finalTagIds = tagIds.length
        ? tagIds
        : tagNames.map(name => findByName(this.tags, name)?.tagId).filter(Boolean);

      const data = await api.searchBookmarks({
        keyword: keyword.trim(),
        tagIds: finalTagIds.length ? finalTagIds : undefined,
        untagged: untagged || undefined,
        page,
        pageSize
      });
      this.setBookmarks(data);
      return this.bookmarks;
    },

    async fetchBookmarkDetail(bookmarkId) {
      const detail = await api.getBookmarkDetail(bookmarkId);
      return this.upsertBookmark(detail);
    },

    async importBookmark(url) {
      const data = await api.importBookmark(url);
      const bookmark = normalizeBookmark({
        bookmarkId: data.bookmarkId,
        link: data.link,
        savedAt: data.savedAt,
        note: '',
        tags: []
      });
      this.upsertBookmark(bookmark);
      return { ...data, bookmark };
    },

    async organizeBookmark(bookmarkId, note, tagNames = []) {
      const tagIds = tagNames.map(name => findByName(this.tags, name)?.tagId).filter(Boolean);
      const data = await api.organizeBookmark(bookmarkId, note, tagIds);
      const current = this.bookmarks.find(item => item.bookmarkId === bookmarkId);
      this.upsertBookmark({
        ...current,
        bookmarkId,
        note: data.note,
        tags: data.tags
      });
      await Promise.all([this.fetchTags(), this.fetchStats()]);
      return data;
    },

    async updateBookmarkNote(bookmarkId, note) {
      const data = await api.updateBookmarkNote(bookmarkId, note);
      const current = this.bookmarks.find(item => item.bookmarkId === bookmarkId);
      this.upsertBookmark({ ...current, bookmarkId, note: data.note });
      return data;
    },

    async updateBookmarkTags(bookmarkId, tagNames) {
      const tagIds = tagNames.map(name => findByName(this.tags, name)?.tagId).filter(Boolean);
      const data = await api.updateBookmarkTags(bookmarkId, tagIds);
      const current = this.bookmarks.find(item => item.bookmarkId === bookmarkId);
      this.upsertBookmark({ ...current, bookmarkId, tags: data.tags });
      await Promise.all([this.fetchTags(), this.fetchStats()]);
      return data;
    },

    async removeBookmarkTag(bookmarkId, tagName) {
      const current = this.bookmarks.find(item => item.bookmarkId === bookmarkId);
      if (!current) return;
      const nextTags = (current.tags || []).filter(name => name !== tagName);
      return this.updateBookmarkTags(bookmarkId, nextTags);
    },

    async addBookmarkTag(bookmarkId, tagName) {
      const current = this.bookmarks.find(item => item.bookmarkId === bookmarkId);
      if (!current) return;
      const nextTags = Array.from(new Set([...(current.tags || []), tagName]));
      return this.updateBookmarkTags(bookmarkId, nextTags);
    },

    async deleteBookmark(bookmarkId) {
      await api.deleteBookmark(bookmarkId);
      this.bookmarks = this.bookmarks.filter(item => item.bookmarkId !== bookmarkId);
      await Promise.all([this.fetchTags(), this.fetchStats()]);
      return true;
    },

    async createTag(tagName, pinned = false) {
      const tag = await api.createTag(tagName.trim(), pinned);
      const normalized = normalizeTag(tag);
      this.tags.push(normalized);
      return normalized;
    },

    async toggleTagPin(tagName) {
      const tag = findByName(this.tags, tagName);
      if (!tag) return null;
      const updated = normalizeTag(await api.updateTagPin(tag.tagId, !tag.pinned));
      const index = this.tags.findIndex(item => item.tagId === tag.tagId);
      if (index >= 0) this.tags.splice(index, 1, updated);
      return updated;
    },

    async renameTag(oldName, newName) {
      const tag = findByName(this.tags, oldName);
      if (!tag) return null;
      const updated = normalizeTag(await api.renameTag(tag.tagId, newName.trim()));
      const index = this.tags.findIndex(item => item.tagId === tag.tagId);
      if (index >= 0) this.tags.splice(index, 1, updated);
      this.bookmarks.forEach(bookmark => {
        if (bookmark.tags?.includes(oldName)) {
          bookmark.tags = bookmark.tags.map(name => (name === oldName ? updated.name : name));
        }
      });
      return updated;
    },

    async deleteTag(tagName) {
      const tag = findByName(this.tags, tagName);
      if (!tag) return false;
      await api.deleteTag(tag.tagId);
      this.tags = this.tags.filter(item => item.tagId !== tag.tagId);
      return true;
    },

    async mergeTag(source, target) {
      const sourceTag = findByName(this.tags, source);
      const targetTag = findByName(this.tags, target);
      if (!sourceTag || !targetTag) return false;
      await api.mergeTag(sourceTag.tagId, targetTag.tagId);
      await Promise.all([this.fetchTags(), this.fetchRecentBookmarks(1, 5), this.fetchStats()]);
      return true;
    }
  }
});
