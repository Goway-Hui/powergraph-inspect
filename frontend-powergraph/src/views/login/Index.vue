<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h2>变电站巡检知识图谱管理系统</h2>
        <p>PowerGraph Inspect System</p>
      </div>
      
      <a-form :model="loginForm" @submit="handleLogin" class="login-form" layout="vertical">
        <a-form-item field="username" :rules="[{ message: '请输入用户名' }]">
          <a-input 
            v-model="loginForm.username" 
            placeholder="请输入用户名" 
            size="large"
            allow-clear
          >
            <template #prefix>
              <icon-user />
            </template>
          </a-input>
        </a-form-item>
        
        <a-form-item field="password" :rules="[{ message: '请输入密码' }]">
          <a-input-password 
            v-model="loginForm.password" 
            placeholder="请输入密码" 
            size="large"
            allow-clear
          >
            <template #prefix>
              <icon-lock />
            </template>
          </a-input-password>
        </a-form-item>
        
        <a-form-item>
          <a-checkbox v-model="rememberMe">记住我</a-checkbox>
        </a-form-item>
        
        <a-form-item>
          <a-button 
            type="primary" 
            html-type="submit" 
            :loading="loading" 
            long
            size="large"
          >
            {{ loading ? '登录中...' : '登录' }}
          </a-button>
        </a-form-item>
      </a-form>
      
      <div class="login-footer">
        <p>© 2025 PowerGraph Inspect System. All rights reserved.</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { Message } from '@arco-design/web-vue';
import { IconUser, IconLock } from '@arco-design/web-vue/es/icon';
import request from '../../api/request';
import { isAuthenticated } from '../../utils/auth';

interface LoginForm {
  username: string;
  password: string;
}

interface LoginResponse {
  code: number;
  message: string;
  data: {
    token: string;
    user: any;
  };
}

// 初始化表单数据
const loginForm = ref<LoginForm>({
  username: '',
  password: ''
});

const rememberMe = ref<boolean>(true);
const loading = ref<boolean>(false);
const router = useRouter();

// 页面加载时检查是否已登录
onMounted(async () => {
  if (isAuthenticated()) {
    // 如果已经登录，跳转到对应角色的首页
    const userStr = localStorage.getItem('user');
    if (userStr) {
      try {
        const user = JSON.parse(userStr);
        switch (user.role) {
          case 'ADMIN':
            await router.push('/admin');
            break;
          case 'INSPECTOR':
            await router.push('/inspector');
            break;
          case 'VIEWER':
            await router.push('/viewer');
            break;
          default:
            await router.push('/');
        }
      } catch (e) {
        console.error('解析用户信息失败:', e);
      }
    }
  }
  
  // 页面加载时尝试填充记住的凭据
  const savedCredentialsStr = localStorage.getItem('savedCredentials');
  if (savedCredentialsStr) {
    try {
      const savedCredentials = JSON.parse(savedCredentialsStr);
      loginForm.value.username = savedCredentials.username || '';
      loginForm.value.password = savedCredentials.password || '';
      rememberMe.value = true;
    } catch (e) {
      console.error('解析保存的凭据失败:', e);
    }
  }
});

// 处理登录
const handleLogin = async () => {
  if (!loginForm.value.username || !loginForm.value.password) {
    Message.warning('请输入用户名和密码');
    return;
  }
  
  loading.value = true;
  
  try {
    // 响应拦截器会处理 code !== 0 的情况，因此这里我们直接使用 LoginResponse 类型
    const response: LoginResponse = await request.post('/auth/login', {
      username: loginForm.value.username,
      password: loginForm.value.password
    });
    
    // 登录成功
    const { token, user } = response.data;
    
    // 保存 token 和用户信息到本地存储
    localStorage.setItem('token', token);
    localStorage.setItem('user', JSON.stringify(user));
    
    // 如果选择了记住我，保存凭据
    if (rememberMe.value) {
      localStorage.setItem('savedCredentials', JSON.stringify({
        username: loginForm.value.username,
        password: loginForm.value.password
      }));
    } else {
      localStorage.removeItem('savedCredentials');
    }
    
    Message.success('登录成功');
    
    // 根据用户角色跳转到相应页面
    switch (user.role) {
      case 'ADMIN':
        await router.push('/admin');
        break;
      case 'INSPECTOR':
        await router.push('/inspector');
        break;
      case 'VIEWER':
        await router.push('/viewer');
        break;
      default:
        await router.push('/');
    }
  } catch (error: any) {
    console.error('登录错误:', error);
    // 响应拦截器将错误信息包装在 error.message 中
    Message.error(error.message || '登录失败，请检查用户名和密码');
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh; /* 精确占满视窗高度 */
  box-sizing: border-box; /* 内边距计入高度，避免溢出 */
  /* 蓝白风格背景：轻柔的蓝色渐变，和首页保持一致 */
  background: linear-gradient(135deg, #f7faff 0%, #eaf1ff 40%, #d9e6ff 100%);
  padding: 20px;
  overflow: hidden; /* 防止出现垂直滚动条 */
}

.login-card {
  width: 100%;
  max-width: 420px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(22, 93, 255, 0.12);
  border: 1px solid #e6f0ff;
  padding: 32px 24px; /* 优化左右内边距，保持更紧凑的排版 */
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h2 {
  color: #165DFF; 
  font-size: 22px;
  font-weight: 600;
  margin-bottom: 6px;
}

.login-header p {
  color: #4e5969;
  font-size: 13px;
}

.login-form {
  margin-bottom: 20px;
}

.login-footer {
  text-align: center;
  color: #86909c;
  font-size: 12px;
}

/* 微调表单与控件间距，提升整体协调感 */
.login-form :deep(.arco-input-wrapper),
.login-form :deep(.arco-input-password-wrapper) {
  border-radius: 8px;
}

.login-form :deep(.arco-btn-primary) {
  background-color: #165DFF;
  border-color: #165DFF;
  border-radius: 8px;
}
</style>
