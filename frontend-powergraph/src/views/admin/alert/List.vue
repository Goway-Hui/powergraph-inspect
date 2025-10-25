<template>
  <div class="page-container">
    <div class="page-header">
      <a-breadcrumb>
        <a-breadcrumb-item>告警管理</a-breadcrumb-item>
        <a-breadcrumb-item>告警列表</a-breadcrumb-item>
      </a-breadcrumb>
      <h1 class="page-title">告警列表</h1>
      <div class="header-actions">
        <a-button @click="refresh"><icon-refresh />刷新</a-button>
      </div>
    </div>

    <a-card class="filters-card" :bordered="false">
      <a-space wrap>
        <a-input v-model="keyword" placeholder="搜索关键词（设备/内容）" allow-clear style="width: 220px">
          <template #prefix>
            <icon-search />
          </template>
        </a-input>
        <a-select v-model="levelFilter" placeholder="级别" allow-clear style="width: 150px">
          <a-option value="低">低</a-option>
          <a-option value="中">中</a-option>
          <a-option value="高">高</a-option>
          <a-option value="严重">严重</a-option>
        </a-select>
        <a-select v-model="statusFilter" placeholder="状态" allow-clear style="width: 150px">
          <a-option value="未处理">未处理</a-option>
          <a-option value="处理中">处理中</a-option>
          <a-option value="已确认">已确认</a-option>
          <a-option value="已关闭">已关闭</a-option>
        </a-select>
        <a-date-picker v-model="dateRange" type="daterange" placeholder="时间范围" style="width: 280px" />
        <a-button type="primary" @click="applyFilters"><icon-search />筛选</a-button>
        <a-button @click="resetFilters"><icon-refresh />重置</a-button>
      </a-space>
    </a-card>

    <a-card class="table-card" :bordered="false">
      <div class="table-toolbar">
        <a-space>
          <a-button :disabled="selectedKeys.length === 0" @click="batchConfirm">批量确认</a-button>
          <a-button :disabled="selectedKeys.length === 0" @click="batchClose">批量关闭</a-button>
          <a-button status="danger" :disabled="selectedKeys.length === 0" @click="batchDelete">批量删除</a-button>
        </a-space>
      </div>
      <a-table
        row-key="id"
        :columns="columns"
        :data="filteredRows"
        :row-selection="rowSelection"
        :pagination="{ pageSize: 10 }"
      >
        <template #level="{ record }">
          <a-tag :color="levelColor(record.level)">{{ record.level }}</a-tag>
        </template>
        <template #status="{ record }">
          <a-tag :color="statusColor(record.status)">{{ record.status }}</a-tag>
        </template>
        <template #actions="{ record }">
          <a-space>
            <a-button size="small" @click="viewDetail(record)">查看</a-button>
            <a-button size="small" type="primary" :disabled="record.status === '已确认'" @click="confirmAlert(record)">确认</a-button>
            <a-button size="small" status="success" :disabled="record.status === '已关闭'" @click="closeAlert(record)">关闭</a-button>
            <a-button size="small" status="danger" @click="deleteAlert(record)">删除</a-button>
          </a-space>
        </template>
      </a-table>
    </a-card>

    <a-drawer v-model:visible="detailVisible" title="告警详情" :width="560">
      <a-descriptions :column="1" bordered>
        <a-descriptions-item label="告警ID">{{ current?.id }}</a-descriptions-item>
        <a-descriptions-item label="告警时间">{{ current?.time }}</a-descriptions-item>
        <a-descriptions-item label="设备名称">{{ current?.device }}</a-descriptions-item>
        <a-descriptions-item label="级别">
          <a-tag :color="levelColor(current?.level || '低')">{{ current?.level }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="statusColor(current?.status || '未处理')">{{ current?.status }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="告警内容">{{ current?.content }}</a-descriptions-item>
        <a-descriptions-item label="处理人">{{ current?.assignee || '-' }}</a-descriptions-item>
      </a-descriptions>
      <template #footer>
        <a-space>
          <a-button type="primary" :disabled="current?.status === '已确认'" @click="confirmAlert(current!)">确认</a-button>
          <a-button status="success" :disabled="current?.status === '已关闭'" @click="closeAlert(current!)">关闭</a-button>
          <a-button status="danger" @click="deleteAlert(current!)">删除</a-button>
        </a-space>
      </template>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { Message } from '@arco-design/web-vue';
import { IconSearch, IconRefresh } from '@arco-design/web-vue/es/icon';
import { listAlerts, searchAlerts, batchUpdateAlertStatus, batchDeleteAlerts, deleteAlert as deleteAlertApi } from '../../../api/alerts';

type Row = {
  id: number;
  time: string;
  device: string;
  level: '低'|'中'|'高'|'严重';
  content: string;
  status: '未处理'|'处理中'|'已确认'|'已关闭';
  assignee?: string;
};

const keyword = ref('');
const levelFilter = ref<string | undefined>();
const statusFilter = ref<string | undefined>();
const dateRange = ref<string[] | undefined>();

const rows = ref<Row[]>([]);

const columns = [
  { title: '告警时间', dataIndex: 'time', width: 160 },
  { title: '设备名称', dataIndex: 'device', width: 140 },
  { title: '级别', dataIndex: 'level', slotName: 'level', width: 100 },
  { title: '告警内容', dataIndex: 'content' },
  { title: '状态', dataIndex: 'status', slotName: 'status', width: 100 },
  { title: '处理人', dataIndex: 'assignee', width: 100 },
  { title: '操作', slotName: 'actions', width: 220 }
];

const selectedKeys = ref<number[]>([]);
const rowSelection = computed(() => ({
  type: 'checkbox',
  selectedRowKeys: selectedKeys.value,
  onChange: (keys: number[]) => { selectedKeys.value = keys; }
}));

const filteredRows = computed(() => {
  return rows.value.filter(r => {
    const matchKeyword = keyword.value ? (r.device.includes(keyword.value) || r.content.includes(keyword.value)) : true;
    const matchLevel = levelFilter.value ? r.level === levelFilter.value : true;
    const matchStatus = statusFilter.value ? r.status === statusFilter.value : true;
    let matchDate = true;
    const start = dateRange.value?.[0];
    const end = dateRange.value?.[1];
    if (start && end) {
      const rt = new Date(r.time.replace(' ', 'T')).getTime();
      const st = new Date(start).getTime();
      const et = new Date(end).getTime();
      matchDate = rt >= st && rt <= et;
    }
    return matchKeyword && matchLevel && matchStatus && matchDate;
  });
});

const levelColor = (lvl: Row['level']) => {
  const map: Record<Row['level'], string> = { '低': 'arcoblue', '中': 'orange', '高': 'red', '严重': 'purple' };
  return map[lvl] || 'arcoblue';
};

const statusColor = (st: Row['status']) => {
  const map: Record<Row['status'], string> = { '未处理': 'red', '处理中': 'orange', '已确认': 'green', '已关闭': 'gray' };
  return map[st] || 'blue';
};

const detailVisible = ref(false);
const current = ref<Row | null>(null);

const viewDetail = (row: Row) => { current.value = row; detailVisible.value = true; };

const confirmAlert = async (row: Row) => {
  try {
    await batchUpdateAlertStatus([row.id], '已确认');
    row.status = '已确认';
    Message.success(`告警 ${row.id} 已确认`);
  } catch (e: any) {
    Message.error(e.message || '确认失败');
  }
};

const closeAlert = async (row: Row) => {
  try {
    await batchUpdateAlertStatus([row.id], '已关闭');
    row.status = '已关闭';
    Message.success(`告警 ${row.id} 已关闭`);
  } catch (e: any) {
    Message.error(e.message || '关闭失败');
  }
};

const deleteAlert = async (row: Row) => {
  try {
    await deleteAlertApi(row.id);
    rows.value = rows.value.filter(r => r.id !== row.id);
    Message.success(`告警 ${row.id} 已删除`);
    detailVisible.value = false;
  } catch (e: any) {
    Message.error(e.message || '删除失败');
  }
};

const batchConfirm = async () => {
  try {
    await batchUpdateAlertStatus(selectedKeys.value, '已确认');
    rows.value = rows.value.map(r => selectedKeys.value.includes(r.id) ? { ...r, status: '已确认' } : r);
    Message.success('已批量确认选中告警');
  } catch (e: any) {
    Message.error(e.message || '批量确认失败');
  }
};

const batchClose = async () => {
  try {
    await batchUpdateAlertStatus(selectedKeys.value, '已关闭');
    rows.value = rows.value.map(r => selectedKeys.value.includes(r.id) ? { ...r, status: '已关闭' } : r);
    Message.success('已批量关闭选中告警');
  } catch (e: any) {
    Message.error(e.message || '批量关闭失败');
  }
};

const batchDelete = async () => {
  try {
    await batchDeleteAlerts(selectedKeys.value);
    rows.value = rows.value.filter(r => !selectedKeys.value.includes(r.id));
    selectedKeys.value = [];
    Message.success('已批量删除选中告警');
  } catch (e: any) {
    Message.error(e.message || '批量删除失败');
  }
};

const applyFilters = async () => {
  try {
    const resp = await searchAlerts({ keyword: keyword.value || undefined, status: statusFilter.value || undefined, level: levelFilter.value || undefined });
    rows.value = ((resp as any).data || []).map((a: any) => ({
      id: a.id!,
      time: a.createdAt || '',
      device: a.deviceId ? `设备#${a.deviceId}` : '-',
      level: (a.level as any) || '低',
      content: a.content || '',
      status: (a.status as any) || '未处理',
      assignee: a.assignedTo ? `用户#${a.assignedTo}` : undefined
    }));
    Message.success('筛选条件已应用');
  } catch (e: any) {
    Message.error(e.message || '筛选失败');
  }
};

const resetFilters = () => { keyword.value = ''; levelFilter.value = undefined; statusFilter.value = undefined; dateRange.value = undefined; };

const refresh = async () => {
  try {
    const resp = await listAlerts();
    rows.value = ((resp as any).data || []).map((a: any) => ({
      id: a.id!,
      time: a.createdAt || '',
      device: a.deviceId ? `设备#${a.deviceId}` : '-',
      level: (a.level as any) || '低',
      content: a.content || '',
      status: (a.status as any) || '未处理',
      assignee: a.assignedTo ? `用户#${a.assignedTo}` : undefined
    }));
    Message.success('列表已刷新');
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
.filters-card { margin-bottom: 12px; }
.table-toolbar { margin-bottom: 12px; display: flex; justify-content: space-between; }
</style>