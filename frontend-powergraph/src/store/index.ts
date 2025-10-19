import { reactive } from 'vue';

// 全局状态管理
interface State {
  user: {
    username: string | null;
    role: string | null;
  };
  theme: 'light' | 'dark';
  locale: 'zh-CN' | 'en-US';
}

const state: State = reactive({
  user: {
    username: null,
    role: null
  },
  theme: 'light',
  locale: 'zh-CN'
});

// 获取用户信息
export const getUser = () => {
  return state.user;
};

// 设置用户信息
export const setUser = (username: string, role: string) => {
  state.user.username = username;
  state.user.role = role;
};

// 清除用户信息
export const clearUser = () => {
  state.user.username = null;
  state.user.role = null;
};

// 获取主题
export const getTheme = () => {
  return state.theme;
};

// 设置主题
export const setTheme = (theme: 'light' | 'dark') => {
  state.theme = theme;
};

// 获取语言
export const getLocale = () => {
  return state.locale;
};

// 设置语言
export const setLocale = (locale: 'zh-CN' | 'en-US') => {
  state.locale = locale;
};

export default state;