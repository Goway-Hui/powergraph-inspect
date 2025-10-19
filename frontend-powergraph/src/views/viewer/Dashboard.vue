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
              <span>运营总览</span>
            </template>
            <a-menu-item key="1-1">运营状态总览</a-menu-item>
          </a-sub-menu>
          
          <a-sub-menu key="2">
            <template #title>
              <icon-line-chart />
              <span>数据分析</span>
            </template>
            <a-menu-item key="2-1">设备状态分析</a-menu-item>
            <a-menu-item key="2-2">告警趋势分析</a-menu-item>
            <a-menu-item key="2-3">任务完成分析</a-menu-item>
          </a-sub-menu>
          
          <a-sub-menu key="3">
            <template #title>
              <icon-file />
              <span>分析报告</span>
            </template>
            <a-menu-item key="3-1">日报</a-menu-item>
            <a-menu-item key="3-2">周报</a-menu-item>
            <a-menu-item key="3-3">月报</a-menu-item>
          </a-sub-menu>
        </a-menu>
      </a-layout-sider>
      
      <a-layout-content class="content">
        <div class="dashboard-header">
          <h1>运营状态总览</h1>
          <a-button @click="refreshData">
            <icon-refresh />
            刷新数据
          </a-button>
        </div>
        
        <!-- 关键运行指标 -->
        <a-row :gutter="16" class="kpi-panel">
          <a-col :span="6">
            <a-card class="kpi-card">
              <a-statistic title="设备总数" :value="128" />
            </a-card>
          </a-col>
          <a-col :span="6">
            <a-card class="kpi-card">
              <a-statistic title="正常率" :value="98.2" suffix="%" />
            </a-card>
          </a-col>
          <a-col :span="6">
            <a-card class="kpi-card">
              <a-statistic title="巡检完成率" :value="95.6" suffix="%" />
            </a-card>
          </a-col>
          <a-col :span="6">
            <a-card class="kpi-card">
              <a-statistic title="告警数量" :value="12" />
            </a-card>
          </a-col>
        </a-row>
        
        <!-- 图表区域 -->
        <a-row :gutter="16" class="chart-area">
          <a-col :span="12">
            <a-card title="设备状态分布" class="chart-card">
              <div ref="deviceStatusChart" class="chart-container"></div>
            </a-card>
          </a-col>
          <a-col :span="12">
            <a-card title="告警级别统计" class="chart-card">
              <div ref="alertLevelChart" class="chart-container"></div>
            </a-card>
          </a-col>
        </a-row>
        
        <!-- 实时告警列表 -->
        <a-card title="实时告警列表" class="alert-list">
          <a-table :columns="alertColumns" :data="alertData" :pagination="{ pageSize: 5 }">
            <template #level="{ record }">
              <a-tag :color="getLevelColor(record.level)">{{ record.level }}</a-tag>
            </template>
            <template #status="{ record }">
              <a-tag :color="record.status === '未处理' ? 'red' : 'green'">{{ record.status }}</a-tag>
            </template>
          </a-table>
        </a-card>
        
        <!-- 分析报告入口 -->
        <div class="report-section">
          <a-button type="primary" @click="goToReports">
            <icon-file />
            查看详细分析报告
          </a-button>
        </div>
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { Message } from '@arco-design/web-vue';
import * as echarts from 'echarts';

const router = useRouter();

// 图表引用
const deviceStatusChart = ref<HTMLElement | null>(null);
const alertLevelChart = ref<HTMLElement | null>(null);

// 告警表格列定义
const alertColumns = [
  {
    title: '告警时间',
    dataIndex: 'time'
  },
  {
    title: '设备名称',
    dataIndex: 'device'
  },
  {
    title: '告警级别',
    dataIndex: 'level',
    slotName: 'level'
  },
  {
    title: '告警内容',
    dataIndex: 'content'
  },
  {
    title: '状态',
    dataIndex: 'status',
    slotName: 'status'
  }
];

// 告警数据
const alertData = ref([
  {
    time: '2025-10-14 10:30',
    device: '主变压器#1',
    level: '高',
    content: '温度异常升高',
    status: '未处理'
  },
  {
    time: '2025-10-14 09:45',
    device: '断路器#3',
    level: '中',
    content: '机械部件磨损',
    status: '已处理'
  },
  {
    time: '2025-10-14 08:20',
    device: '电压互感器#2',
    level: '低',
    content: '绝缘性能下降',
    status: '处理中'
  },
  {
    time: '2025-10-14 07:15',
    device: '避雷器#1',
    level: '高',
    content: '接地电阻超标',
    status: '未处理'
  },
  {
    time: '2025-10-14 06:30',
    device: '电容器组#2',
    level: '中',
    content: '电容值异常',
    status: '已处理'
  }
]);

// 获取告警级别颜色
const getLevelColor = (level: string) => {
  const colorMap: Record<string, string> = {
    '低': 'blue',
    '中': 'orange',
    '高': 'red',
    '严重': 'purple'
  };
  return colorMap[level] || 'blue';
};

// 初始化图表
const initCharts = () => {
  if (deviceStatusChart.value) {
    const deviceChart = echarts.init(deviceStatusChart.value);
    deviceChart.setOption({
      tooltip: {
        trigger: 'item'
      },
      legend: {
        top: '5%',
        left: 'center'
      },
      series: [
        {
          name: '设备状态',
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: {
            show: false,
            position: 'center'
          },
          emphasis: {
            label: {
              show: true,
              fontSize: '18',
              fontWeight: 'bold'
            }
          },
          labelLine: {
            show: false
          },
          data: [
            { value: 125, name: '正常' },
            { value: 2, name: '异常' },
            { value: 1, name: '维修中' }
          ]
        }
      ]
    });
  }

  if (alertLevelChart.value) {
    const alertChart = echarts.init(alertLevelChart.value);
    alertChart.setOption({
      tooltip: {
        trigger: 'axis'
      },
      legend: {
        data: ['低', '中', '高', '严重']
      },
      xAxis: {
        type: 'category',
        data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          name: '低',
          type: 'bar',
          stack: '总量',
          data: [12, 15, 10, 8, 11, 9, 13]
        },
        {
          name: '中',
          type: 'bar',
          stack: '总量',
          data: [5, 3, 4, 6, 2, 7, 4]
        },
        {
          name: '高',
          type: 'bar',
          stack: '总量',
          data: [2, 1, 3, 2, 1, 0, 2]
        },
        {
          name: '严重',
          type: 'bar',
          stack: '总量',
          data: [0, 0, 1, 0, 0, 0, 1]
        }
      ]
    });
  }
};

// 刷新数据
const refreshData = () => {
  Message.success('数据已刷新');
};

// 跳转到分析报告
const goToReports = () => {
  Message.info('跳转到分析报告页面');
};

// 退出登录
const handleLogout = () => {
  Message.success('已退出登录');
  router.push('/login');
};

// 组件挂载后初始化图表
onMounted(() => {
  initCharts();
});
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

.kpi-card {
  text-align: center;
}

.chart-area {
  margin-bottom: 20px;
}

.chart-card {
  height: 100%;
}

.chart-container {
  height: 300px;
}

.alert-list {
  margin-bottom: 20px;
}

.report-section {
  text-align: center;
}

</style>