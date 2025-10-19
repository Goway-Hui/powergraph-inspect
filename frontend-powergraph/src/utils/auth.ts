// 用户权限相关工具函数

// 检查用户是否已登录
export const isAuthenticated = (): boolean => {
  const token = localStorage.getItem('token');
  if (!token) return false;
  
  // 可以添加token过期检查逻辑
  return true;
};

// 获取用户角色
export const getUserRole = (): string | null => {
  const userStr = localStorage.getItem('user');
  if (userStr) {
    try {
      const user = JSON.parse(userStr);
      return user.role || null;
    } catch (e) {
      console.error('解析用户信息失败:', e);
      return null;
    }
  }
  return null;
};

// 检查用户是否有指定角色
export const hasRole = (roles: string[]): boolean => {
  const userRole = getUserRole();
  if (!userRole) return false;
  return roles.includes(userRole);
};

// 获取用户信息
export const getUserInfo = (): any => {
  const userStr = localStorage.getItem('user');
  if (userStr) {
    try {
      return JSON.parse(userStr);
    } catch (e) {
      console.error('解析用户信息失败:', e);
      return null;
    }
  }
  return null;
};

// 登出
export const logout = (): void => {
  localStorage.removeItem('token');
  localStorage.removeItem('user');
  localStorage.removeItem('savedCredentials');
  
  // 跳转到登录页
  window.location.href = '/login';
};