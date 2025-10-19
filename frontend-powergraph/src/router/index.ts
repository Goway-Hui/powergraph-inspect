import { createRouter, createWebHistory } from 'vue-router';
import routes from './routes';
import { isAuthenticated, getUserRole } from '../utils/auth';

const router = createRouter({
  history: createWebHistory(),
  routes: routes
});

// 全局路由守卫
router.beforeEach((to, _from, next) => {
  // 如果目标路由需要认证
  if (to.meta.requiresAuth) {
    // 检查用户是否已登录
    if (!isAuthenticated()) {
      // 未登录则重定向到登录页
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      });
      return;
    }
    
    // 如果路由需要特定角色
    if (to.meta.role) {
      const userRole = getUserRole();
      // 检查用户角色是否匹配
      if (userRole !== to.meta.role) {
        // 角色不匹配，重定向到登录页或无权限页面
        next({
          path: '/login',
          query: { redirect: to.fullPath }
        });
        return;
      }
    }
  }
  
  // 对于不需要认证的路由或者权限验证通过的路由，允许访问
  next();
});

export default router;