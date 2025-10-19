import axios from 'axios';

// 创建 axios 实例
const request = axios.create({
  baseURL: '/api', // 后端 API 基础路径
  timeout: 10000, // 请求超时时间
  headers: {
    'Content-Type': 'application/json'
  }
});

// 请求拦截器
request.interceptors.request.use(
  (config: any) => {
    // 在发送请求之前做些什么
    // 如果是登录请求，则不添加 token
    if (config.url === '/auth/login') {
      return config;
    }
    
    // 对其他请求，添加 token
    const token = localStorage.getItem('token');
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error: any) => {
    // 对请求错误做些什么
    return Promise.reject(error);
  }
);

// 响应拦截器
request.interceptors.response.use(
  (response: any) => {
    // 对响应数据做点什么
    const { code, message } = response.data;
    
    // 根据后端约定的状态码进行处理
    if (code === 0) {
      // 成功
      return response.data;
    } else {
      // 失败
      console.error('API Error:', message);
      return Promise.reject(new Error(message || 'Error'));
    }
  },
  (error: any) => {
    // 对响应错误做点什么
    console.error('Network/Error:', error);
    // 优先处理鉴权相关错误
    if (error.response) {
      const status = error.response.status;
      if (status === 401) {
        try {
          // 清除本地登录信息并跳转登录页
          localStorage.removeItem('token');
          localStorage.removeItem('user');
        } catch {}
        const redirect = encodeURIComponent(window.location.pathname + window.location.search);
        if (typeof window !== 'undefined') {
          window.location.href = `/login?redirect=${redirect}`;
        }
        return Promise.reject(new Error('未登录或登录已过期，请重新登录'));
      }
      // 其他可识别的服务端错误信息
      const serverMsg = (error.response.data && (error.response.data.message || error.response.data.error)) || '';
      return Promise.reject(new Error(serverMsg || `请求失败(${status})`));
    }
    // 无响应（例如网络断开、跨域失败）
    return Promise.reject(new Error('网络错误，请检查网络连接'));
  }
);

export default request;