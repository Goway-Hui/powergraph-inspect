<template>
  <div class="page-container">
    <div class="page-header">
      <a-breadcrumb>
        <a-breadcrumb-item>告警管理</a-breadcrumb-item>
        <a-breadcrumb-item>告警统计</a-breadcrumb-item>
      </a-breadcrumb>
      <h1 class="page-title">告警统计</h1>
      <div class="header-actions">
        <a-button @click="refresh"><icon-refresh />刷新</a-button>
      </div>
    </div>

    <a-row :gutter="16" class="kpi-panel">
      <a-col :xs="24" :sm="6">
        <a-card class="kpi-card" :bordered="false">
          <a-statistic title="告警总数" :value="stats.total" />
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="6">
        <a-card class="kpi-card" :bordered="false">
          <a-statistic title="高/严重级别" :value="stats.high" />
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="6">
        <a-card class="kpi-card" :bordered="false">
          <a-statistic title="已确认" :value="stats.ack" />
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="6">
        <a-card class="kpi-card" :bordered="false">
          <a-statistic title="已关闭" :value="stats.closed" />
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="16" class="chart-area">
      <a-col :xs="24" :md="12">
        <a-card title="告警级别分布" class="chart-card" :bordered="false">
          <div ref="levelChart" class="chart-container"></div>
        </a-card>
      </a-col>
      <a-col :xs="24" :md="12">
        <a-card title="近7日告警趋势" class="chart-card" :bordered="false">
          <div ref="trendChart" class="chart-container"></div>
        </a-card>
      </a-col>
    </a-row>

    <a-card title="近期告警明细" :bordered="false" class="table-card">
      <a-table :columns="columns" :data="rows" row-key="id">
        <template #level="{ record }">
          <a-tag :color="levelColor(record.level)">{{ record.level }}</a-tag>
        </template>
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
import { listAlerts } from '../../../api/alerts';

type Row = { id: number; time: string; device: string; level: '低'|'中'|'高'|'严重'; content: string; status: '未处理'|'处理中'|'已确认'|'已关闭'; };

const rows = ref<Row[]>([]);

const stats = computed(() => ({
  total: rows.value.length,
  high: rows.value.filter(r => r.level === '高' || r.level === '严重').length,
  ack: rows.value.filter(r => r.status === '已确认').length,
  closed: rows.value.filter(r => r.status === '已关闭').length
}));

const columns = [
  { title: '告警时间', dataIndex: 'time', width: 160 },
  { title: '设备名称', dataIndex: 'device', width: 140 },
  { title: '级别', dataIndex: 'level', slotName: 'level', width: 100 },
  { title: '告警内容', dataIndex: 'content' },
  { title: '状态', dataIndex: 'status', slotName: 'status', width: 120 }
];

const levelColor = (lvl: Row['level']) => {
  const map: Record<Row['level'], string> = { '低': 'arcoblue', '中': 'orange', '高': 'red', '严重': 'purple' };
  return map[lvl] || 'arcoblue';
};
const statusColor = (st: Row['status']) => {
  const map: Record<Row['status'], string> = { '未处理': 'red', '处理中': 'orange', '已确认': 'green', '已关闭': 'gray' };
  return map[st] || 'blue';
};

const levelChart = ref<HTMLElement | null>(null);
const trendChart = ref<HTMLElement | null>(null);

const renderCharts = () => {
  if (levelChart.value) {
    const chart = echarts.init(levelChart.value);
    const levelCounts = ['低','中','高','严重'].map(l => rows.value.filter(r => r.level === (l as any)).length);
    chart.setOption({
      tooltip: { trigger: 'item' },
      legend: { bottom: 0 },
      series: [{
        type: 'pie',
        radius: '60%',
        data: [
          { name: '低', value: levelCounts[0] },
          { name: '中', value: levelCounts[1] },
          { name: '高', value: levelCounts[2] },
          { name: '严重', value: levelCounts[3] }
        ]
      }]
    });
  }
  if (trendChart.value) {
    const chart = echarts.init(trendChart.value);
    chart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: ['-6','-5','-4','-3','-2','-1','今天'] },
      yAxis: { type: 'value' },
      series: [{ type: 'line', data: [8, 5, 6, 9, 7, 4, 10] }]
    });
  }
};

const refresh = async () => {
  try {
    const resp = await listAlerts();
    const list = resp.data || [];
    rows.value = (list as unknown as any[]).map((a: any) => ({
      id: a.id!,
      time: a.createdAt || '',
      device: a.deviceId ? `设备#${a.deviceId}` : '-',
      level: (a.level as any) || '低',
      content: a.content || '',
      status: (a.status as any) || '未处理'
    }));
    Message.success('统计数据已刷新');
    nextTick(renderCharts);
  } catch (e: any) {
    Message.error(e.message || '加载失败');
  }
};

onMounted(refresh);
</script>

<style scoped>
.page-container { padding: 16px; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.page-title { font-size: 18px; font-weight: 600; margin: 8px 0; }
.kpi-panel { margin-bottom: 12px; }
.chart-area { margin-bottom: 12px; }
.chart-container { width: 100%; height: 280px; }
.table-card { }
</style>