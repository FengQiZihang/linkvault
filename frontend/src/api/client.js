const TOKEN_KEY = 'linkvault_access_token';

const getDefaultBaseUrl = () => {
  // 1. 如果配置了环境变量 VITE_API_BASE_URL，则优先使用
  if (import.meta.env?.VITE_API_BASE_URL) {
    return import.meta.env.VITE_API_BASE_URL;
  }

  // 2. 本地日常测试：在 H5 浏览器开发环境下，返回 '/api'，使用 Vite 代理转发到本地 localhost:8080
  // #ifdef H5
  if (process.env.NODE_ENV === 'development') {
    return '/api';
  }
  // #endif

  // 3. 手机云打包部署或真机测试：直接使用 CF 内网穿透域名访问本地后端
  return 'https://lv.fengqizihang.me';
};

export const API_BASE_URL = getDefaultBaseUrl();

export const getAccessToken = () => uni.getStorageSync(TOKEN_KEY) || '';

export const setAccessToken = (token) => {
  if (token) {
    uni.setStorageSync(TOKEN_KEY, token);
  } else {
    uni.removeStorageSync(TOKEN_KEY);
  }
};

const buildUrl = (path, query = {}) => {
  const queryPairs = Object.entries(query)
    .filter(([, value]) => value !== undefined && value !== null && value !== '')
    .map(([key, value]) => {
      const finalValue = Array.isArray(value) ? value.join(',') : value;
      return `${encodeURIComponent(key)}=${encodeURIComponent(finalValue)}`;
    });

  const normalizedPath = path.startsWith('/') ? path : `/${path}`;
  const queryString = queryPairs.length ? `?${queryPairs.join('&')}` : '';
  return `${API_BASE_URL}${normalizedPath}${queryString}`;
};

export const request = ({ url, method = 'GET', data, query, auth = true }) => {
  const headers = {
    'Content-Type': 'application/json'
  };

  const token = getAccessToken();
  if (auth && token) {
    headers.Authorization = `Bearer ${token}`;
  }

  return new Promise((resolve, reject) => {
    uni.request({
      url: buildUrl(url, query),
      method,
      data,
      header: headers,
      success: (response) => {
        const body = response.data || {};
        const statusCode = response.statusCode;

        if (statusCode >= 200 && statusCode < 300 && body.code === 0) {
          resolve(body.data);
          return;
        }

        const error = new Error(body.msg || `请求失败 (${statusCode})`);
        error.statusCode = statusCode;
        error.code = body.code;
        error.data = body.data;

        if (statusCode === 401 || body.code === 40101 || body.code === 40102) {
          setAccessToken('');
          uni.removeStorageSync('linkvault_user_info');
        }

        reject(error);
      },
      fail: (err) => {
        reject(new Error(err.errMsg || '网络请求失败'));
      }
    });
  });
};

export const api = {
  sendSmsCode: (phone) => request({
    url: '/auth/sms-code',
    method: 'POST',
    auth: false,
    data: { phone }
  }),
  login: (phone, code) => request({
    url: '/auth/login',
    method: 'POST',
    auth: false,
    data: { phone, code }
  }),
  getMe: () => request({ url: '/me' }),
  updateMe: (payload) => request({ url: '/me', method: 'PUT', data: payload }),
  getStats: () => request({ url: '/me/stats' }),
  importBookmark: (url) => request({ url: '/bookmarks/import', method: 'POST', data: { url } }),
  listRecentBookmarks: (query) => request({ url: '/bookmarks', query }),
  getBookmarkDetail: (bookmarkId) => request({ url: `/bookmarks/${bookmarkId}` }),
  updateBookmarkNote: (bookmarkId, note) => request({
    url: `/bookmarks/${bookmarkId}/note`,
    method: 'PUT',
    data: { note }
  }),
  deleteBookmark: (bookmarkId) => request({ url: `/bookmarks/${bookmarkId}`, method: 'DELETE' }),
  updateBookmarkTags: (bookmarkId, tagIds) => request({
    url: `/bookmarks/${bookmarkId}/tags`,
    method: 'PUT',
    data: { tagIds }
  }),
  organizeBookmark: (bookmarkId, note, tagIds) => request({
    url: `/bookmarks/${bookmarkId}/organize`,
    method: 'PUT',
    data: { note, tagIds }
  }),
  searchBookmarks: (query) => request({ url: '/bookmarks/search', query }),
  listTags: () => request({ url: '/tags' }),
  createTag: (name, pinned = false) => request({
    url: '/tags',
    method: 'POST',
    data: { name, pinned }
  }),
  renameTag: (tagId, name) => request({
    url: `/tags/${tagId}/name`,
    method: 'PATCH',
    data: { name }
  }),
  updateTagPin: (tagId, pinned) => request({
    url: `/tags/${tagId}/pin`,
    method: 'PATCH',
    data: { pinned }
  }),
  deleteTag: (tagId) => request({ url: `/tags/${tagId}`, method: 'DELETE' }),
  mergeTag: (sourceTagId, targetTagId) => request({
    url: `/tags/${sourceTagId}/merge`,
    method: 'POST',
    data: { targetTagId }
  })
};
