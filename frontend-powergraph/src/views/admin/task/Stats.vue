<template>
  <div class="page-container">
    <div class="page-header">
      <a-breadcrumb>
        <a-breadcrumb-item>任务管理</a-breadcrumb-item>
        <a-breadcrumb-item>任务统计</a-breadcrumb-item>
      </a-breadcrumb>
      <h1 class="page-title">任务统计</h1>
      <div class="header-actions">
        <a-button @click="refresh"><icon-refresh />刷新</a-button>
      </div>
    </div>

    <a-row :gutter="16" class="kpi-panel">
      <a-col :xs="24" :sm="6">
        <a-card class="kpi-card" :bordered="false">
          <a-statistic title="总任务数" :value="stats.total" />
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="6">
        <a-card class="kpi-card" :bordered="false">
          <a-statistic title="进行中" :value="stats.inProgress" />
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="6">
        <a-card class="kpi-card" :bordered="false">
          <a-statistic title="已完成" :value="stats.done" />
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="6">
        <a-card class="kpi-card" :bordered="false">
          <a-statistic title="待分配" :value="stats.unassigned" />
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="16" class="chart-area">
      <a-col :xs="24" :sm="12">
        <a-card title="任务状态分布" class="chart-card" :bordered="false">
          <div ref="statusChart" class="chart-container"></div>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12">
        <a-card title="巡检员任务占比" class="chart-card" :bordered="false">
          <div ref="assigneeChart" class="chart-container"></div>
        </a-card>
      </a-col>
    </a-row>

    <a-card title="近期任务明细" :bordered="false" class="table-card">
      <a-table :columns="columns" :data="rows" row-key="id">
        <template #status="{ record }">
          <a-tag :color="statusColor(record.status)">{{ record.status }}</a-tag>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, nextTick } from 'vue';
import * as echarts from 'echarts';
import { Message } from '@arco-design/web-vue';
import { IconRefresh } from '@arco-design/web-vue/es/icon';

type Row = { id: number; title: string; assignee: string; status: '待分配'|'进行中'|'已完成'|'已取消'; };

const rows = ref<Row[]>([
  { id: 1, title: '主变压器温度巡检', assignee: '张三', status: '进行中' },
  { id: 2, title: '断路器开关测试', assignee: '李四', status: '待分配' },
  { id: 3, title: '环网柜外观检查', assignee: '王五', status: '已完成' },
  { id: 4, title: '传感器数据校准', assignee: '赵六', status: '进行中' },
  { id: 5, title: '电缆通断检测', assignee: '张三', status: '已取消' }
]);

const stats = computed(() => ({
  total: rows.value.length,
  inProgress: rows.value.filter(r => r.status === '进行中').length,
  done: rows.value.filter(r => r.status === '已完成').length,
  unassigned: rows.value.filter(r => r.status === '待分配').length
}));

const columns = [
  { title: '任务名称', dataIndex: 'title' },
  { title: '巡检员', dataIndex: 'assignee', width: 120 },
  { title: '状态', slotName: 'status', width: 120 }
];

const statusColor = (s: Row['status']) => {
  if (s === '进行中') return 'arcoblue';
  if (s === '待分配') return 'orange';
  if (s === '已完成') return 'green';
  return 'red';
};

const statusChart = ref<HTMLDivElement | null>(null);
const assigneeChart = ref<HTMLDivElement | null>(null);

const renderCharts = () => {
  if (statusChart.value) {
    const chart = echarts.init(statusChart.value);
    const counts = ['待分配','进行中','已完成','已取消'].map(s => rows.value.filter(r => r.status === s).length);
    chart.setOption({
      tooltip: {},
      series: [{
        type: 'pie',
        data: [
          { value: counts[0], name: '待分配' },
          { value: counts[1], name: '进行中' },
          { value: counts[2], name: '已完成' },
          { value: counts[3], name: '已取消' }
        ]
      }]
    });
  }
  if (assigneeChart.value) {
    const chart = echarts.init(assigneeChart.value);
    const assignees = Array.from(new Set(rows.value.map(r => r.assignee)));
    const values = assignees.map(a => rows.value.filter(r => r.assignee === a).length);
    chart.setOption({
      tooltip: {},
      xAxis: { type: 'category', data: assignees },
      yAxis: { type: 'value' },
      series: [{ type: 'bar', data: values }]
    });
  }
};

onMounted(() => { nextTick(renderCharts); });

const refresh = () => { Message.success('统计数据已刷新'); renderCharts(); };
</script>

<style scoped>
.page-container { padding: 16px; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.page-title { font-size: 18px; font-weight: 600; margin: 8px 0; }
.kpi-panel { margin-bottom: 12px; }
.chart-area { margin-bottom: 12px; }
.chart-card { }
.chart-container { width: 100%; height: 280px; }
</style>