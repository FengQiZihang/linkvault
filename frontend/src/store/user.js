import { defineStore } from 'pinia';
import { api, getAccessToken, setAccessToken } from '@/api/client.js';

const USER_KEY = 'linkvault_user_info';
const PROFILE_REQUIRED_KEY = 'linkvault_profile_required';

export const AVATAR_OPTIONS = [
  { emoji: '🦊', url: '/static/avatars/avatar-01.png' },
  { emoji: '🌙', url: '/static/avatars/avatar-02.png' },
  { emoji: '⚡', url: '/static/avatars/avatar-03.png' },
  { emoji: '🚀', url: '/static/avatars/avatar-04.png' },
  { emoji: '🎧', url: '/static/avatars/avatar-05.png' },
  { emoji: '💎', url: '/static/avatars/avatar-06.png' },
  { emoji: '🌿', url: '/static/avatars/avatar-07.png' },
  { emoji: '🔥', url: '/static/avatars/avatar-08.png' },
  { emoji: '⭐', url: '/static/avatars/avatar-09.png' }
];

export const avatarUrlToEmoji = (avatarUrl) => {
  return AVATAR_OPTIONS.find(item => item.url === avatarUrl)?.emoji || '🦊';
};

export const avatarEmojiToUrl = (emoji) => {
  return AVATAR_OPTIONS.find(item => item.emoji === emoji)?.url || AVATAR_OPTIONS[0].url;
};

const normalizeUser = (user) => {
  if (!user) return null;
  return {
    ...user,
    avatarUrl: user.avatarUrl || AVATAR_OPTIONS[0].url,
    avatar: user.avatar || avatarUrlToEmoji(user.avatarUrl)
  };
};

export const useUserStore = defineStore('user', {
  state: () => ({
    accessToken: getAccessToken(),
    userInfo: normalizeUser(uni.getStorageSync(USER_KEY) || null),
    profileRequired: uni.getStorageSync(PROFILE_REQUIRED_KEY) === true,
    loading: false
  }),

  getters: {
    isLoggedIn: (state) => !!state.accessToken && !!state.userInfo,
    isProfileSetup: (state) => !!state.userInfo && !state.profileRequired
  },

  actions: {
    persistUser(user) {
      this.userInfo = normalizeUser(user);
      if (this.userInfo) {
        uni.setStorageSync(USER_KEY, this.userInfo);
      } else {
        uni.removeStorageSync(USER_KEY);
      }
    },

    persistProfileRequired(required) {
      this.profileRequired = !!required;
      uni.setStorageSync(PROFILE_REQUIRED_KEY, this.profileRequired);
    },

    async sendCode(phone) {
      return api.sendSmsCode(phone);
    },

    async login(phone, code) {
      if (!phone || !code) {
        throw new Error('手机号和验证码不能为空');
      }

      this.loading = true;
      try {
        const session = await api.login(phone, code);
        this.accessToken = session.accessToken;
        setAccessToken(session.accessToken);
        this.persistUser(session.user);
        this.persistProfileRequired(!!session.isNewUser);
        return session;
      } finally {
        this.loading = false;
      }
    },

    async fetchCurrentUser() {
      if (!this.accessToken) return null;

      const user = await api.getMe();
      this.persistUser(user);
      return this.userInfo;
    },

    async updateProfile(nickname, avatar) {
      if (!this.accessToken) {
        throw new Error('当前无登录态，更新失败');
      }

      const updatedUser = await api.updateMe({
        nickname,
        avatarUrl: avatarEmojiToUrl(avatar)
      });
      this.persistUser(updatedUser);
      this.persistProfileRequired(false);
      return this.userInfo;
    },

    logout() {
      this.accessToken = '';
      this.persistUser(null);
      this.persistProfileRequired(false);
      setAccessToken('');

      uni.reLaunch({
        url: '/pages/login/login'
      });
    }
  }
});
