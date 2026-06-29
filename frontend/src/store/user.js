import { defineStore } from 'pinia';
import { api, getAccessToken, setAccessToken } from '@/api/client.js';

const USER_KEY = 'linkvault_user_info';
const PROFILE_REQUIRED_KEY = 'linkvault_profile_required';

export const AVATAR_OPTIONS = [
  { id: 'avatar-01', url: '/static/avatars/avatar-01.png', svgUrl: '/static/avatars/avatar-01.svg', label: '阳光猫' },
  { id: 'avatar-02', url: '/static/avatars/avatar-02.png', svgUrl: '/static/avatars/avatar-02.svg', label: '薄荷机器人' },
  { id: 'avatar-03', url: '/static/avatars/avatar-03.png', svgUrl: '/static/avatars/avatar-03.svg', label: '浆果狐' },
  { id: 'avatar-04', url: '/static/avatars/avatar-04.png', svgUrl: '/static/avatars/avatar-04.svg', label: '丁香熊猫' },
  { id: 'avatar-05', url: '/static/avatars/avatar-05.png', svgUrl: '/static/avatars/avatar-05.svg', label: '珊瑚兔' },
  { id: 'avatar-06', url: '/static/avatars/avatar-06.png', svgUrl: '/static/avatars/avatar-06.svg', label: '海洋鸟' },
  { id: 'avatar-07', url: '/static/avatars/avatar-07.png', svgUrl: '/static/avatars/avatar-07.svg', label: '青柠外星人' },
  { id: 'avatar-08', url: '/static/avatars/avatar-08.png', svgUrl: '/static/avatars/avatar-08.svg', label: '蜜桃恐龙' },
  { id: 'avatar-09', url: '/static/avatars/avatar-09.png', svgUrl: '/static/avatars/avatar-09.svg', label: '暗夜忍者' }
];

export const avatarUrlToSvg = (avatarUrl) => {
  if (!avatarUrl) return AVATAR_OPTIONS[0].svgUrl;
  const found = AVATAR_OPTIONS.find(item => item.url === avatarUrl || item.svgUrl === avatarUrl || item.id === avatarUrl);
  return found ? found.svgUrl : AVATAR_OPTIONS[0].svgUrl;
};

export const avatarSvgToUrl = (svgUrlOrId) => {
  const found = AVATAR_OPTIONS.find(item => item.svgUrl === svgUrlOrId || item.id === svgUrlOrId || item.url === svgUrlOrId);
  return found ? found.url : AVATAR_OPTIONS[0].url;
};

const normalizeUser = (user) => {
  if (!user) return null;
  const svgUrl = avatarUrlToSvg(user.avatarUrl);
  return {
    ...user,
    avatarUrl: user.avatarUrl,
    avatarSvg: svgUrl
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
        avatarUrl: avatarSvgToUrl(avatar)
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
