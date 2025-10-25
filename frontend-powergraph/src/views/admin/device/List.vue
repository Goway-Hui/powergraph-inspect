<template>
  <div class="page-container">
    <div class="page-header">
      <a-breadcrumb>
        <a-breadcrumb-item>设备管理</a-breadcrumb-item>
        <a-breadcrumb-item>设备列表</a-breadcrumb-item>
      </a-breadcrumb>
      <h1 class="page-title">设备列表</h1>
      <div class="header-actions">
        <a-button type="primary" @click="openCreate">
          <icon-plus />
          新建设备
        </a-button>
        <a-button @click="refresh">
          <icon-refresh />
          刷新
        </a-button>
      </div>
    </div>

    <a-card class="filters-card" :bordered="false">
      <a-row :gutter="12" align="center">
        <a-col :xs="24" :sm="12" :md="8">
          <a-input-search v-model="keyword" placeholder="搜索设备名称/编号/变电站" allow-clear @search="applyFilters">
            <template #prefix><icon-search /></template>
          </a-input-search>
        </a-col>
        <a-col :xs="12" :sm="6" :md="4">
          <a-select v-model="statusFilter" placeholder="设备状态" allow-clear @change="applyFilters">
            <a-option value="正常">正常</a-option>
            <a-option value="告警">告警</a-option>
            <a-option value="维护">维护</a-option>
          </a-select>
        </a-col>
        <a-col :xs="12" :sm="6" :md="4">
          <a-select v-model="categoryFilter" placeholder="设备分类" allow-clear @change="applyFilters">
            <a-option v-for="c in categories" :key="c" :value="c">{{ c }}</a-option>
          </a-select>
        </a-col>
      </a-row>
    </a-card>

    <a-card class="table-card" :bordered="false">
      <a-table :columns="columns" :data="pagedData" :pagination="pagination" :loading="loading" row-key="id">
        <template #status="{ record }">
          <a-tag :color="statusColor(record.status)">{{ record.status }}</a-tag>
        </template>
        <template #actions="{ record }">
          <a-space>
            <a-button type="text" size="small" @click="viewDevice(record)">查看</a-button>
            <a-button type="text" size="small" @click="editDevice(record)">编辑</a-button>
            <a-popconfirm content="确认删除该设备？" @ok="removeDevice(record.id)">
              <a-button type="text" size="small" status="danger">删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </a-table>
    </a-card>

    <a-modal v-model:visible="editVisible" :title="editing ? '编辑设备' : '新建设备'" @ok="saveDevice" @cancel="closeEdit">
      <a-form :model="editForm" layout="vertical">
        <a-form-item label="设备名称" field="name" :rules="[{ required: true, message: '请输入设备名称' }]">
          <a-input v-model="editForm.name" placeholder="请输入设备名称" />
        </a-form-item>
        <a-form-item label="设备编号" field="code" :rules="[{ required: true, message: '请输入设备编号' }]">
          <a-input v-model="editForm.code" placeholder="请输入设备编号" />
        </a-form-item>
        <a-form-item label="设备分类" field="category" :rules="[{ required: true, message: '请选择设备分类' }]">
          <a-select v-model="editForm.category" placeholder="请选择">
            <a-option v-for="c in categories" :key="c" :value="c">{{ c }}</a-option>
          </a-select>
        </a-form-item>
        <a-form-item label="所属变电站" field="station" :rules="[{ required: false }]">
          <a-input v-model="editForm.station" placeholder="请输入所属变电站编号(可选)" />
        </a-form-item>
        <a-form-item label="设备状态" field="status" :rules="[{ required: true, message: '请选择设备状态' }]">
          <a-select v-model="editForm.status" placeholder="请选择">
            <a-option value="正常">正常</a-option>
            <a-option value="告警">告警</a-option>
            <a-option value="维护">维护</a-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { Message } from '@arco-design/web-vue';
import { 
  IconPlus, 
  IconRefresh, 
  IconSearch 
} from '@arco-design/web-vue/es/icon';
import type { DeviceDTO } from '../../../api/devices';
import { listDevices, createDevice, updateDevice, deleteDeviceApi } from '../../../api/devices';

// 表格行类型（与UI展示一致）
type DeviceRow = {
  id: number;
  name: string;
  code: string;
  category: string;
  station: string;
  status: '正常' | '告警' | '维护';
  lastInspection: string;
};

const categories = ref<string[]>(['变压器','断路器','开关柜','电缆','传感器']);

// 设备列表改为后端加载
const devices = ref<DeviceRow[]>([]);
const loading = ref<boolean>(false);

const keyword = ref('');
const statusFilter = ref<string | undefined>();
const categoryFilter = ref<string | undefined>();

const filteredData = computed(() => {
  const kw = keyword.value.trim().toLowerCase();
  return devices.value.filter(d => {
    const kwMatch = !kw || [d.name, d.code, d.station].some(v => v.toLowerCase().includes(kw));
    const statusMatch = !statusFilter.value || d.status === statusFilter.value;
    const categoryMatch = !categoryFilter.value || d.category === categoryFilter.value;
    return kwMatch && statusMatch && categoryMatch;
  });
});

const pagination = reactive({
  pageSize: 8,
  current: 1,
  showTotal: true,
  onChange: (p: number) => { pagination.current = p; }
});

const pagedData = computed(() => {
  const start = (pagination.current - 1) * pagination.pageSize;
  return filteredData.value.slice(start, start + pagination.pageSize);
});

const columns = [
  { title: '设备名称', dataIndex: 'name' },
  { title: '设备编号', dataIndex: 'code' },
  { title: '设备分类', dataIndex: 'category' },
  { title: '所属变电站', dataIndex: 'station' },
  { title: '状态', slotName: 'status' },
  { title: '最近巡检', dataIndex: 'lastInspection' },
  { title: '操作', slotName: 'actions', width: 180 }
];

const statusColor = (s: DeviceRow['status']) => {
  if (s === '正常') return 'green';
  if (s === '告警') return 'red';
  return 'orange';
};

const editVisible = ref(false);
const editing = ref(false);
const editForm = reactive<DeviceRow>({ id: 0, name: '', code: '', category: '', station: '', status: '正常', lastInspection: '' });

const applyFilters = async () => {
  pagination.current = 1;
  await loadList();
};

const refresh = async () => {
  await loadList();
  Message.success('列表已刷新');
};

const openCreate = () => {
  editing.value = false;
  Object.assign(editForm, { id: 0, name: '', code: '', category: '', station: '', status: '正常', lastInspection: '' });
  editVisible.value = true;
};

const viewDevice = (record: DeviceRow) => {
  Message.info(`查看设备：${record.name}`);
};

const editDevice = (record: DeviceRow) => {
  editing.value = true;
  Object.assign(editForm, record);
  editVisible.value = true;
};

const closeEdit = () => {
  editVisible.value = false;
};

const toRow = (d: DeviceDTO): DeviceRow => ({
  id: d.id as number,
  name: d.name,
  code: d.code || '',
  category: d.type || '',
  station: d.stationId != null ? `站点#${d.stationId}` : (d.location || ''),
  status: (d.status as DeviceRow['status']) || '正常',
  lastInspection: d.lastInspectionAt ? d.lastInspectionAt.slice(0, 10) : '—'
});

const loadList = async () => {
  try {
    loading.value = true;
    const resp = await listDevices({
      keyword: keyword.value || undefined,
      status: statusFilter.value || undefined,
      type: categoryFilter.value || undefined
    });
    const rows = (resp.data || []).map(toRow);
    devices.value = rows;
  } catch (e: any) {
    Message.error(e?.message || '设备列表加载失败');
  } finally {
    loading.value = false;
  }
};

const saveDevice = async () => {
  if (!editForm.name || !editForm.code || !editForm.category) {
    Message.error('请完善设备信息');
    return;
  }
  const payload: DeviceDTO = {
    name: editForm.name,
    code: editForm.code,
    type: editForm.category,
    status: editForm.status,
    // stationId 可选：尝试从输入中解析数字
    stationId: editForm.station && /^\d+$/.test(editForm.station) ? Number(editForm.station) : undefined
  };
  try {
    if (editing.value) {
      await updateDevice(editForm.id, payload);
      Message.success('设备已更新');
    } else {
      await createDevice(payload);
      Message.success('设备已创建');
    }
    editVisible.value = false;
    await loadList();
  } catch (e: any) {
    Message.error(e?.message || (editing.value ? '设备更新失败' : '设备创建失败'));
  }
};

const removeDevice = async (id: number) => {
  try {
    await deleteDeviceApi(id);
    Message.success('设备已删除');
    await loadList();
  } catch (e: any) {
    Message.error(e?.message || '设备删除失败');
  }
};

onMounted(() => {
  loadList();
});
</script>

<style scoped>
.page-container { padding: 16px; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.page-title { font-size: 18px; font-weight: 600; margin: 8px 0; }
.header-actions { display: flex; gap: 8px; }
.filters-card { margin-bottom: 12px; }
.table-card { }
</style>