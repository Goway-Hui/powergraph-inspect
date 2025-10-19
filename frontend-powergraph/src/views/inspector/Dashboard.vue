<template>
  <a-layout class="layout">
    <a-layout-header class="header">
      <div class="logo">变电站巡检知识图谱管理系统</div>
      <div class="user-info">
        <a-dropdown>
          <a-avatar :size="32" style="background-color: #3370ff;">
            <icon-user />
          </a-avatar>
          <template #content>
            <a-doption>
              <icon-settings />
              <span>个人设置</span>
            </a-doption>
            <a-doption @click="handleLogout">
              <icon-export />
              <span>退出登录</span>
            </a-doption>
          </template>
        </a-dropdown>
      </div>
    </a-layout-header>
    
    <a-layout>
      <a-layout-sider class="sider" collapsible>
        <a-menu
          :default-open-keys="['1']"
          :default-selected-keys="['1']"
          :style="{ height: '100%' }"
        >
          <a-sub-menu key="1">
            <template #title>
              <icon-dashboard />
              <span>工作台</span>
            </template>
            <a-menu-item key="1-1">我的工作台</a-menu-item>
          </a-sub-menu>
          
          <a-sub-menu key="2">
            <template #title>
              <icon-check-circle />
              <span>任务管理</span>
            </template>
            <a-menu-item key="2-1">我的任务</a-menu-item>
            <a-menu-item key="2-2">任务历史</a-menu-item>
          </a-sub-menu>
          
          <a-sub-menu key="3">
            <template #title>
              <icon-bulb />
              <span>异常管理</span>
            </template>
            <a-menu-item key="3-1">异常上报</a-menu-item>
            <a-menu-item key="3-2">异常记录</a-menu-item>
          </a-sub-menu>
          
          <a-sub-menu key="4">
            <template #title>
              <icon-book />
              <span>学习资料</span>
            </template>
            <a-menu-item key="4-1">操作手册</a-menu-item>
            <a-menu-item key="4-2">培训视频</a-menu-item>
          </a-sub-menu>
        </a-menu>
      </a-layout-sider>
      
      <a-layout-content class="content">
        <div class="dashboard-header">
          <h1>我的工作台</h1>
          <a-button type="primary" @click="showReportModal = true">
            <icon-plus />
            异常上报
          </a-button>
        </div>
        
        <!-- 个人绩效看板 -->
        <a-row :gutter="16" class="kpi-panel">
          <a-col :span="8">
            <a-statistic-card title="今日完成率" :value="85" suffix="%" />
          </a-col>
          <a-col :span="8">
            <a-statistic-card title="本周完成率" :value="92" suffix="%" />
          </a-col>
          <a-col :span="8">
            <a-statistic-card title="本月完成率" :value="88" suffix="%" />
          </a-col>
        </a-row>
        
        <!-- 任务状态 -->
        <a-card title="我的待办任务" class="task-list">
          <a-tabs default-active-key="1">
            <a-tab-pane key="1" title="紧急任务">
              <a-table :columns="taskColumns" :data="urgentTasks" :pagination="false" />
            </a-tab-pane>
            <a-tab-pane key="2" title="进行中">
              <a-table :columns="taskColumns" :data="inProgressTasks" :pagination="false" />
            </a-tab-pane>
            <a-tab-pane key="3" title="待执行">
              <a-table :columns="taskColumns" :data="pendingTasks" :pagination="false" />
            </a-tab-pane>
          </a-tabs>
        </a-card>
        
        <!-- 系统公告 -->
        <a-card title="系统公告" class="announcements">
          <a-timeline>
            <a-timeline-item v-for="item in announcements" :key="item.id">
              <a-alert :type="getAlertType(item.type)" :title="item.title" :content="item.content" />
            </a-timeline-item>
          </a-timeline>
        </a-card>
      </a-layout-content>
    </a-layout>
    
    <!-- 异常上报模态框 -->
    <a-modal v-model:visible="showReportModal" title="异常上报" @ok="handleReport" @cancel="showReportModal = false">
      <a-form :model="reportForm" layout="vertical">
        <a-form-item field="device" label="设备名称">
          <a-input v-model="reportForm.device" placeholder="请输入设备名称" />
        </a-form-item>
        <a-form-item field="level" label="异常等级">
          <a-select v-model="reportForm.level" placeholder="请选择异常等级">
            <a-option value="low">低</a-option>
            <a-option value="medium">中</a-option>
            <a-option value="high">高</a-option>
            <a-option value="critical">严重</a-option>
          </a-select>
        </a-form-item>
        <a-form-item field="description" label="异常描述">
          <a-textarea v-model="reportForm.description" placeholder="请输入异常描述" />
        </a-form-item>
      </a-form>
    </a-modal>
  </a-layout>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { Message } from '@arco-design/web-vue';

const router = useRouter();
const showReportModal = ref(false);

const reportForm = reactive({
  device: '',
  level: '',
  description: ''
});

const taskColumns = [
  {
    title: '任务编号',
    dataIndex: 'id'
  },
  {
    title: '设备名称',
    dataIndex: 'device'
  },
  {
    title: '任务内容',
    dataIndex: 'content'
  },
  {
    title: '截止时间',
    dataIndex: 'deadline'
  },
  {
    title: '操作',
    slotName: 'action'
  }
];

const urgentTasks = ref([
  {
    id: 'T001',
    device: '主变压器#1',
    content: '温度异常检查',
    deadline: '2025-10-15 10:00'
  },
  {
    id: 'T002',
    device: '断路器#3',
    content: '机械部件检查',
    deadline: '2025-10-15 14:00'
  }
]);

const inProgressTasks = ref([
  {
    id: 'T003',
    device: '电压互感器#2',
    content: '绝缘性能检测',
    deadline: '2025-10-16 09:00'
  }
]);

const pendingTasks = ref([
  {
    id: 'T004',
    device: '避雷器#1',
    content: '接地电阻测试',
    deadline: '2025-10-17 11:00'
  },
  {
    id: 'T005',
    device: '电容器组#2',
    content: '电容值测量',
    deadline: '2025-10-17 15:00'
  }
]);

const announcements = ref([
  {
    id: 1,
    type: 'info',
    title: '系统维护通知',
    content: '系统将于本周六凌晨2:00-4:00进行维护升级，请提前做好工作安排。'
  },
  {
    id: 2,
    type: 'warning',
    title: '安全提醒',
    content: '请各位巡检人员注意个人防护，严格按照操作规程执行任务。'
  },
  {
    id: 3,
    type: 'success',
    title: '优秀员工表彰',
    content: '恭喜张三同志在上月巡检工作中表现突出，被评为优秀员工。'
  }
]);

const getAlertType = (type: string) => {
  const typeMap: Record<string, any> = {
    info: 'info',
    warning: 'warning',
    success: 'success',
    error: 'error'
  };
  return typeMap[type] || 'info';
};

const handleLogout = () => {
  Message.success('已退出登录');
  router.push('/login');
};

const handleReport = () => {
  if (!reportForm.device || !reportForm.level || !reportForm.description) {
    Message.error('请填写完整信息');
    return;
  }
  
  Message.success('异常上报成功');
  showReportModal.value = false;
  
  // 重置表单
  reportForm.device = '';
  reportForm.level = '';
  reportForm.description = '';
};
</script>

<style scoped>
.layout {
  height: 100vh;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #ffffff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 0 20px;
}

.logo {
  font-size: 18px;
  font-weight: bold;
  color: #3370ff;
}

.user-info {
  cursor: pointer;
}

.sider {
  background: #ffffff;
}

.content {
  padding: 20px;
  overflow: auto;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.kpi-panel {
  margin-bottom: 20px;
}

.task-list,
.announcements {
  margin-bottom: 20px;
}
</style>