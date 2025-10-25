<template>
  <a-layout class="layout">
    <a-layout-header class="header">
      <div class="logo" @click="goToAdminHome">变电站巡检知识图谱管理系统</div>
      <div class="header-center">
        <a-input-search 
          placeholder="搜索设备、任务或用户..." 
          style="width: 300px; margin: 0 20px;"
          allow-clear
        >
          <template #prefix>
            <icon-search />
          </template>
        </a-input-search>
      </div>
      <div class="header-actions">
        <a-tooltip content="消息通知">
          <a-button class="header-btn" shape="circle">
            <icon-notification />
            <a-badge dot :count="3" />
          </a-button>
        </a-tooltip>
        <a-tooltip content="系统设置">
          <a-button class="header-btn" shape="circle" @click="handleSettings">
            <icon-settings />
          </a-button>
        </a-tooltip>
        <a-dropdown>
          <a-avatar class="user-avatar" :size="32">
            <img alt="头像" :src="userInfo.avatar || defaultAvatar" />
          </a-avatar>
          <template #content>
            <div class="user-dropdown-header">
              <a-avatar :size="48" class="user-dropdown-avatar">
                <img alt="头像" :src="userInfo.avatar || defaultAvatar" />
              </a-avatar>
              <div class="user-dropdown-info">
                <div class="user-dropdown-name">{{ userInfo.name || '管理员' }}</div>
                <div class="user-dropdown-role">{{ userInfo.role === 'ADMIN' ? '系统管理员' : userInfo.role === 'INSPECTOR' ? '巡检员' : '观察员' }}</div>
              </div>
            </div>
            <a-doption @click="handleProfile">
              <icon-user />
              <span>个人中心</span>
            </a-doption>
            <a-doption @click="handleSettings">
              <icon-settings />
              <span>系统设置</span>
            </a-doption>
            <a-divider style="margin: 4px 0;" />
            <a-doption @click="handleLogout">
              <icon-export />
              <span>退出登录</span>
            </a-doption>
          </template>
        </a-dropdown>
      </div>
    </a-layout-header>
    
    <a-layout class="main-layout">
      <a-layout-sider class="sider" collapsible v-model:collapsed="collapsed">
        <div class="logo-placeholder" v-if="!collapsed">
          <icon-thunderbolt />
          <span @click="goToAdminHome">电力巡检系统</span>
        </div>
        <div class="logo-placeholder-collapsed" v-else>
          <icon-thunderbolt />
        </div>
        <a-menu
          :default-open-keys="['1']"
          :default-selected-keys="['1-1']"
          :style="{ height: 'calc(100% - 60px)' }"
          :collapsed="collapsed"
        >
          <a-sub-menu key="1">
            <template #title>
              <icon-dashboard />
              <span>仪表盘</span>
            </template>
            <a-menu-item key="1-1" @click="goToAdminHome">综合管理仪表盘</a-menu-item>
          </a-sub-menu>
          
          <a-sub-menu key="2">
            <template #title>
              <icon-storage />
              <span>设备管理</span>
            </template>
            <a-menu-item key="2-1" @click="() => router.push('/admin/devices')">设备列表</a-menu-item>
            <a-menu-item key="2-2" @click="() => router.push('/admin/devices/categories')">设备分类</a-menu-item>
            <a-menu-item key="2-3" @click="() => router.push('/admin/devices/status')">设备状态</a-menu-item>
          </a-sub-menu>
          
          <a-sub-menu key="3">
            <template #title>
              <icon-check-circle />
              <span>任务管理</span>
            </template>
            <a-menu-item key="3-1" @click="() => router.push('/admin/tasks')">任务列表</a-menu-item>
            <a-menu-item key="3-2" @click="() => router.push('/admin/tasks/assign')">任务分配</a-menu-item>
            <a-menu-item key="3-3" @click="() => router.push('/admin/tasks/stats')">任务统计</a-menu-item>
          </a-sub-menu>
          
          <!-- 告警管理菜单绑定路由 -->
          <a-sub-menu key="4">
            <template #title>
              <icon-bulb />
              <span>告警管理</span>
            </template>
             <a-menu-item key="4-1" @click="() => router.push('/admin/alerts')">告警列表</a-menu-item>
             <a-menu-item key="4-2" @click="() => router.push('/admin/alerts/rules')">告警规则</a-menu-item>
             <a-menu-item key="4-3" @click="() => router.push('/admin/alerts/stats')">告警统计</a-menu-item>
          </a-sub-menu>
          
          <a-sub-menu key="5">
            <template #title>
              <icon-user />
              <span>用户管理</span>
            </template>
            <a-menu-item key="5-1" @click="() => router.push('/admin/users')">用户列表</a-menu-item>
            <a-menu-item key="5-2" @click="() => router.push('/admin/roles')">角色权限</a-menu-item>
            <a-menu-item key="5-3" @click="() => router.push('/admin/departments')">部门管理</a-menu-item>
          </a-sub-menu>
          
          <a-sub-menu key="6">
            <template #title>
              <icon-mind-mapping />
              <span>报表统计</span>
            </template>
            <a-menu-item key="6-1">运营报表</a-menu-item>
            <a-menu-item key="6-2">设备统计</a-menu-item>
            <a-menu-item key="6-3">巡检报告</a-menu-item>
          </a-sub-menu>
          
          <a-sub-menu key="7">
            <template #title>
              <icon-file />
              <span>知识管理</span>
            </template>
            <a-menu-item key="7-1">知识库</a-menu-item>
            <a-menu-item key="7-2">故障案例</a-menu-item>
            <a-menu-item key="7-3">技术文档</a-menu-item>
          </a-sub-menu>
          
          <a-sub-menu key="8">
            <template #title>
              <icon-mind-mapping />
              <span>知识图谱</span>
            </template>
            <a-menu-item key="8-1" @click="goToGraphVisualization">图谱可视化</a-menu-item>
            <a-menu-item key="8-2" @click="goToGraphQuery">图谱查询</a-menu-item>
            <a-menu-item key="8-3" @click="goToGraphAnalysis">图谱分析</a-menu-item>
          </a-sub-menu>
        </a-menu>
      </a-layout-sider>
      
      <a-layout-content class="content">
        <router-view v-slot="{ Component }">
          <component :is="Component" v-if="Component" />
          <div v-else>
            <div class="dashboard-header">
              <div class="header-info">
                <a-breadcrumb>
                  <a-breadcrumb-item>首页</a-breadcrumb-item>
                  <a-breadcrumb-item>仪表盘</a-breadcrumb-item>
                </a-breadcrumb>
                <h1 class="page-title">综合管理仪表盘</h1>
              </div>
              <div class="header-actions">
                <a-button type="primary" @click="showQuickActionModal = true">
                  <icon-plus />
                  快速操作
                </a-button>
                <a-button @click="refreshData">
                  <icon-refresh />
                  刷新
                </a-button>
              </div>
            </div>
            
            <!-- KPI 面板 -->
            <a-row :gutter="16" class="kpi-panel">
              <a-col :xs="24" :sm="12" :md="6">
                <a-card class="kpi-card" :bordered="false">
                  <div class="kpi-content">
                    <div class="kpi-icon">
                      <icon-storage />
                    </div>
                    <div class="kpi-info">
                      <div class="kpi-title">设备总数</div>
                      <div class="kpi-value">128</div>
                      <div class="kpi-trend">
                        <icon-arrow-rise style="color: #00b42a;" />
                        <span style="color: #00b42a;">2.5%</span>
                      </div>
                    </div>
                  </div>
                </a-card>
              </a-col>
              <a-col :xs="24" :sm="12" :md="6">
                <a-card class="kpi-card" :bordered="false">
                  <div class="kpi-content">
                    <div class="kpi-icon success">
                      <icon-check-circle />
                    </div>
                    <div class="kpi-info">
                      <div class="kpi-title">正常率</div>
                      <div class="kpi-value">98.2<span class="kpi-unit">%</span></div>
                      <div class="kpi-trend">
                        <icon-arrow-rise style="color: #00b42a;" />
                        <span style="color: #00b42a;">1.2%</span>
                      </div>
                    </div>
                  </div>
                </a-card>
              </a-col>
              <a-col :xs="24" :sm="12" :md="6">
                <a-card class="kpi-card" :bordered="false">
                  <div class="kpi-content">
                    <div class="kpi-icon warning">
                      <icon-bulb />
                    </div>
                    <div class="kpi-info">
                      <div class="kpi-title">告警量</div>
                      <div class="kpi-value">12</div>
                      <div class="kpi-trend">
                        <icon-arrow-fall style="color: #f53f3f;" />
                        <span style="color: #f53f3f;">5.3%</span>
                      </div>
                    </div>
                  </div>
                </a-card>
              </a-col>
              <a-col :xs="24" :sm="12" :md="6">
                <a-card class="kpi-card" :bordered="false">
                  <div class="kpi-content">
                    <div class="kpi-icon info">
                      <icon-check-circle />
                    </div>
                    <div class="kpi-info">
                      <div class="kpi-title">任务完成率</div>
                      <div class="kpi-value">95.6<span class="kpi-unit">%</span></div>
                      <div class="kpi-trend">
                        <icon-arrow-rise style="color: #00b42a;" />
                        <span style="color: #00b42a;">3.1%</span>
                      </div>
                    </div>
                  </div>
                </a-card>
              </a-col>
            </a-row>
            
            <!-- 图表和数据展示区域 -->
            <a-row :gutter="16" class="main-content">
              <a-col :xs="24" :sm="24" :md="16">
                <a-card title="系统状态面板" class="system-status-card" :bordered="false">
                  <template #extra>
                    <a-button type="text">查看更多</a-button>
                  </template>
                  <a-row :gutter="16">
                    <a-col :span="12">
                      <a-descriptions :column="1" bordered size="small">
                        <a-descriptions-item label="在线用户">
                          <a-tag color="arcoblue">24</a-tag>
                        </a-descriptions-item>
                        <a-descriptions-item label="核心服务">
                          <a-tag color="green">正常</a-tag>
                        </a-descriptions-item>
                        <a-descriptions-item label="数据库状态">
                          <a-tag color="green">正常</a-tag>
                        </a-descriptions-item>
                      </a-descriptions>
                    </a-col>
                    <a-col :span="12">
                      <a-descriptions :column="1" bordered size="small">
                        <a-descriptions-item label="API网关">
                          <a-tag color="green">正常</a-tag>
                        </a-descriptions-item>
                        <a-descriptions-item label="存储空间">
                          <a-tag color="arcoblue">78%</a-tag>
                        </a-descriptions-item>
                        <a-descriptions-item label="系统负载">
                          <a-tag color="orange">中等</a-tag>
                        </a-descriptions-item>
                      </a-descriptions>
                    </a-col>
                  </a-row>
                </a-card>
                
                <a-card title="知识图谱概览" class="graph-overview-card" :bordered="false" :body-style="{ padding: '0' }">
                  <template #extra>
                    <a-button type="text">全屏查看</a-button>
                  </template>
                  <div class="graph-container">
                    <div class="graph-header">
                      <div class="graph-title">变电站设备关联图谱</div>
                      <div class="graph-actions">
                        <a-button type="text" size="small" @click="previewZoomIn">
                          <icon-zoom-in />
                        </a-button>
                        <a-button type="text" size="small" @click="previewZoomOut">
                          <icon-zoom-out />
                        </a-button>
                        <a-button type="text" size="small" @click="previewFit">
                          <icon-fullscreen />
                        </a-button>
                      </div>
                    </div>
                    <div class="graph-content">
                      <GraphPreview ref="graphPreviewRef" />
                      <div class="graph-overlay-actions">
                        <a-button type="primary" size="small" @click="goToGraphVisualization">进入图谱分析</a-button>
                      </div>
                    </div>
                  </div>
                </a-card>
              </a-col>
              
              <a-col :xs="24" :sm="24" :md="8">
                <a-card title="待办任务列表" class="todo-list-card" :bordered="false">
                  <template #extra>
                    <a-button type="text">查看全部</a-button>
                  </template>
                  <a-list :data="todoList" :bordered="false">
                    <template #item="{ item }">
                      <a-list-item>
                        <a-list-item-meta :title="item.title" :description="item.description">
                        </a-list-item-meta>
                        <template #actions>
                          <a-button type="text" size="small" @click="handleTask(item)">处理</a-button>
                        </template>
                      </a-list-item>
                    </template>
                  </a-list>
                </a-card>
                
                <a-card title="系统公告" class="notice-card" :bordered="false">
                  <a-list :data="notices" :bordered="false">
                    <template #item="{ item }">
                      <a-list-item>
                        <a-list-item-meta :title="item.title" :description="item.date">
                          <template #avatar>
                            <a-avatar :style="{ backgroundColor: item.color }">{{ item.avatar }}</a-avatar>
                          </template>
                        </a-list-item-meta>
                      </a-list-item>
                    </template>
                  </a-list>
                </a-card>
              </a-col>
            </a-row>
          </div>
        </router-view>
      </a-layout-content>
    </a-layout>
    
    <!-- 快速操作模态框 -->
    <a-modal v-model:visible="showQuickActionModal" title="快速操作" @ok="handleQuickAction" @cancel="showQuickActionModal = false">
      <a-space direction="vertical" style="width: 100%;">
        <a-button long>
          <icon-plus />
          新建设备
        </a-button>
        <a-button long>
          <icon-check-circle />
          分配任务
        </a-button>
        <a-button long>
          <icon-user />
          用户管理
        </a-button>
        <a-button long>
          <icon-bulb />
          告警处理
        </a-button>
        <a-button long>
          <icon-file />
          生成报告
        </a-button>
      </a-space>
    </a-modal>
  </a-layout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { Message } from '@arco-design/web-vue';
import GraphPreview from '../graph/Preview.vue';
import { 
  IconNotification, 
  IconSettings, 
  IconUser, 
  IconExport, 
  IconDashboard, 
  IconStorage, 
  IconCheckCircle, 
  IconBulb, 
  IconFile, 
  IconPlus, 
  IconRefresh,
  IconArrowRise,
  IconArrowFall,
  IconSearch,
  IconZoomIn,
  IconZoomOut,
  IconFullscreen
} from '@arco-design/web-vue/es/icon';
import { IconThunderbolt } from '@arco-design/web-vue/es/icon';
import { IconMindMapping } from '@arco-design/web-vue/es/icon';

const router = useRouter();
const showQuickActionModal = ref(false);
const collapsed = ref(false);
const defaultAvatar = 'https://avatars.githubusercontent.com/u/123456';

// 用户信息
const userInfo = reactive({
  name: '系统管理员',
  role: 'ADMIN',
  avatar: 'https://avatars.githubusercontent.com/u/123456'
});

// 待办任务列表
const todoList = ref([
  {
    id: 1,
    title: '新用户审核',
    description: '待审核用户：张三',
    status: 'processing',
    statusText: '处理中'
  },
  {
    id: 2,
    title: '设备告警',
    description: '变压器温度过高',
    status: 'error',
    statusText: '紧急'
  },
  {
    id: 3,
    title: '任务分配',
    description: '今日新增巡检任务',
    status: 'warning',
    statusText: '待处理'
  },
  {
    id: 4,
    title: '系统维护',
    description: '数据库备份',
    status: 'success',
    statusText: '已完成'
  }
]);

// 系统公告
const notices = ref([
  {
    id: 1,
    title: '系统升级通知',
    date: '2025-10-10',
    color: '#165DFF',
    avatar: '通'
  },
  {
    id: 2,
    title: '新功能上线',
    date: '2025-10-08',
    color: '#00B42A',
    avatar: '功'
  },
  {
    id: 3,
    title: '安全提醒',
    date: '2025-10-05',
    color: '#FF7D00',
    avatar: '安'
  }
]);

// 获取用户信息
const getUserInfo = () => {
  const userStr = localStorage.getItem('user');
  if (userStr) {
    try {
      const user = JSON.parse(userStr);
      userInfo.name = user.name || userInfo.name;
      userInfo.role = user.role || userInfo.role;
      userInfo.avatar = user.avatar || userInfo.avatar;
    } catch (e) {
      console.error('解析用户信息失败:', e);
    }
  }
};

// 刷新数据
const refreshData = () => {
  Message.success('数据刷新成功');
};

// 处理任务
const handleTask = (task: any) => {
  Message.info(`处理任务: ${task.title}`);
};

// 个人中心
const handleProfile = () => {
  Message.info('进入个人中心');
};

// 系统设置
const handleSettings = () => {
  Message.info('进入系统设置');
};

// 退出登录
const handleLogout = () => {
  // 清除本地存储
  localStorage.removeItem('token');
  localStorage.removeItem('user');
  Message.success('已退出登录');
  router.push('/login');
};

  // 跳转到图谱可视化页面
  const goToGraphVisualization = () => {
    router.push('/admin/graph');
  };

// 返回综合管理仪表盘首页
const goToAdminHome = () => {
  router.push('/admin');
};

// 跳转到图谱查询页面
const goToGraphQuery = () => {
  Message.info('跳转到图谱查询页面');
  // TODO: 实现图谱查询页面路由
};

  // 跳转到图谱分析页面
  const goToGraphAnalysis = () => {
    Message.info('跳转到图谱分析页面');
    // TODO: 实现图谱分析页面路由
  };

// 图谱预览操作按钮（顶部的放大/缩小/全屏）
const graphPreviewRef = ref<any>(null);
const previewZoomIn = () => { graphPreviewRef.value?.zoomIn?.(); };
const previewZoomOut = () => { graphPreviewRef.value?.zoomOut?.(); };
const previewFit = () => { graphPreviewRef.value?.fit?.(); };
// 在图谱操作按钮区域添加“重新布局”按钮并绑定 previewRelayout 方法

// 快速操作
const handleQuickAction = () => {
  Message.info('执行快速操作');
  showQuickActionModal.value = false;
};

// 组件挂载时获取用户信息
onMounted(() => {
  getUserInfo();
});
</script>

<style scoped>
.layout {
  height: 100vh;
  width: 100vw;
  position: fixed;
  top: 0;
  left: 0;
  overflow: hidden;
}

.header {
  height: 48px;  
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #ffffff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 0 20px;
  z-index: 100;
  flex: 0 0 auto;
}

.logo {
  font-size: 18px;
  font-weight: bold;
  color: #3370ff;
  white-space: nowrap;
}

.header-center {
  flex: 1;
  display: flex;
  justify-content: center;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-btn {
  color: #4e5969;
  position: relative;
}

.header-btn:hover {
  background-color: #f2f3f5;
}

.user-avatar {
  cursor: pointer;
}

.user-dropdown-header {
  display: flex;
  align-items: center;
  padding: 12px 16px;
}

.user-dropdown-avatar {
  margin-right: 12px;
}

.user-dropdown-info {
  display: flex;
  flex-direction: column;
}

.user-dropdown-name {
  font-weight: 500;
  font-size: 14px;
  line-height: 20px;
  color: #1d2129;
}

.user-dropdown-role {
  font-size: 12px;
  line-height: 16px;
  color: #86909c;
}

.main-layout {
  flex: 1;
  overflow: hidden;
}

.sider {
  background: #ffffff;
  box-shadow: 2px 0 8px 0 rgba(0, 0, 0, 0.08);
  z-index: 99;
  height: 100%;
}

.logo-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 60px;
  font-size: 16px;
  font-weight: 600;
  color: #165dff;
  border-bottom: 1px solid #f2f3f5;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.logo-placeholder-collapsed {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 60px;
  font-size: 20px;
  color: #165dff;
  border-bottom: 1px solid #f2f3f5;
}

.logo-placeholder svg {
  margin-right: 8px;
}

.content {
  padding: 20px;
  background-color: #f5f6f7;
  height: calc(100vh - 48px);
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 16px;
}

.header-info {
  flex: 1;
}

.page-title {
  margin: 8px 0;
  font-size: 24px;
  font-weight: 500;
  color: #1d2129;
}

.kpi-panel {
  margin-bottom: 20px;
}

.kpi-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.2s ease;
  height: 100%;
}

.kpi-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

.kpi-content {
  display: flex;
  align-items: center;
  padding: 16px;
}

.kpi-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  font-size: 24px;
  color: #fff;
  background-color: #165dff;
}

.kpi-icon.success {
  background-color: #00b42a;
}

.kpi-icon.warning {
  background-color: #ff7d00;
}

.kpi-icon.info {
  background-color: #165dff;
}

.kpi-info {
  flex: 1;
}

.kpi-title {
  font-size: 14px;
  color: #86909c;
  margin-bottom: 4px;
}

.kpi-value {
  font-size: 24px;
  font-weight: 600;
  color: #1d2129;
  margin-bottom: 4px;
}

.kpi-unit {
  font-size: 14px;
  font-weight: normal;
  margin-left: 2px;
}

.kpi-trend {
  display: flex;
  align-items: center;
  font-size: 12px;
  font-weight: 500;
}

.system-status-card,
.graph-overview-card,
.todo-list-card,
.notice-card {
  margin-bottom: 16px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.graph-container {
  height: 500px;
  display: flex;
  flex-direction: column;
}

.graph-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #f2f3f5;
}

.graph-title {
  font-size: 16px;
  font-weight: 500;
  color: #1d2129;
}

.graph-actions {
  display: flex;
  gap: 8px;
}

.graph-content {
  flex: 1;
  overflow: hidden;
}

.graph-placeholder {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100%;
  color: #86909c;
  background-color: #fafafa;
}

.graph-placeholder svg {
  font-size: 48px;
  margin-bottom: 10px;
  color: #c9cdd4;
}

:deep(.arco-menu-inline-header) {
  display: flex;
  align-items: center;
}

:deep(.arco-menu-inline-header svg) {
  margin-right: 8px;
}

:deep(.arco-badge) {
  position: absolute;
  top: 0;
  right: 0;
  transform: translate(50%, -50%);
}
</style>