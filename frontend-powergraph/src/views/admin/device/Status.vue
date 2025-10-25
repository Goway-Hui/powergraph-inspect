<template>
  <div class="page-container">
    <div class="page-header">
      <a-breadcrumb>
        <a-breadcrumb-item>设备管理</a-breadcrumb-item>
        <a-breadcrumb-item>设备状态</a-breadcrumb-item>
      </a-breadcrumb>
      <h1 class="page-title">设备状态</h1>
      <div class="header-actions">
        <a-button @click="refresh"><icon-refresh />刷新</a-button>
      </div>
    </div>

    <a-row :gutter="16" class="kpi-panel">
      <a-col :xs="24" :sm="8">
        <a-card class="kpi-card" :bordered="false">
          <a-statistic title="正常设备" :value="stats.normal" />
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="8">
        <a-card class="kpi-card" :bordered="false">
          <a-statistic title="告警设备" :value="stats.alert" />
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="8">
        <a-card class="kpi-card" :bordered="false">
          <a-statistic title="维护中设备" :value="stats.maintain" />
        </a-card>
      </a-col>
    </a-row>

    <a-card class="filters-card" :bordered="false">
      <a-space>
        <a-select v-model="statusFilter" placeholder="筛选状态" allow-clear style="width: 160px">
          <a-option value="正常">正常</a-option>
          <a-option value="告警">告警</a-option>
          <a-option value="维护">维护</a-option>
        </a-select>
        <a-input-search v-model="keyword" placeholder="搜索设备名称/变电站" allow-clear style="width: 240px" @search="applyFilters">
          <template #prefix><icon-search /></template>
        </a-input-search>
      </a-space>
    </a-card>

    <a-card class="table-card" :bordered="false">
      <a-table :columns="columns" :data="filteredData" row-key="id">
        <template #status="{ record }">
          <a-tag :color="statusColor(record.status)">{{ record.status }}</a-tag>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { Message } from '@arco-design/web-vue';
import { IconRefresh, IconSearch } from '@arco-design/web-vue/es/icon';

type DeviceRow = { id: number; name: string; station: string; status: '正常'|'告警'|'维护'; };

const rows = ref<DeviceRow[]>([
  { id: 1, name: '主变压器 A', station: '110kV-南站', status: '正常' },
  { id: 2, name: '主变压器 B', station: '110kV-北站', status: '告警' },
  { id: 3, name: '高压断路器 C', station: '220kV-东站', status: '维护' },
  { id: 4, name: '环网柜 D', station: '110kV-西站', status: '正常' },
  { id: 5, name: '温度传感器 E', station: '220kV-东站', status: '告警' }
]);

const stats = computed(() => ({
  normal: rows.value.filter(r => r.status === '正常').length,
  alert: rows.value.filter(r => r.status === '告警').length,
  maintain: rows.value.filter(r => r.status === '维护').length
}));

const statusFilter = ref<string | undefined>();
const keyword = ref('');

const filteredData = computed(() => {
  const kw = keyword.value.trim().toLowerCase();
  return rows.value.filter(r => {
    const kwMatch = !kw || [r.name, r.station].some(v => v.toLowerCase().includes(kw));
    const statusMatch = !statusFilter.value || r.status === statusFilter.value;
    return kwMatch && statusMatch;
  });
});

const columns = [
  { title: '设备名称', dataIndex: 'name' },
  { title: '所属变电站', dataIndex: 'station' },
  { title: '状态', slotName: 'status', width: 120 }
];

const statusColor = (s: DeviceRow['status']) => {
  if (s === '正常') return 'green';
  if (s === '告警') return 'red';
  return 'orange';
};

const applyFilters = () => {};
const refresh = () => { Message.success('状态数据已刷新'); };
</script>

<style scoped>
.page-container { padding: 16px; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.page-title { font-size: 18px; font-weight: 600; margin: 8px 0; }
.kpi-panel { margin-bottom: 12px; }
.filters-card { margin-bottom: 12px; }
</style>