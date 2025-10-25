<template>
  <div class="page-container">
    <div class="page-header">
      <a-breadcrumb>
        <a-breadcrumb-item>用户管理</a-breadcrumb-item>
        <a-breadcrumb-item>角色权限</a-breadcrumb-item>
      </a-breadcrumb>
      <h1 class="page-title">角色权限管理</h1>
      <div class="header-actions">
        <a-button type="primary" @click="openCreate"><icon-plus />新增角色</a-button>
        <a-button @click="refresh"><icon-refresh />刷新</a-button>
      </div>
    </div>

    <a-card class="filters-card" :bordered="false">
      <a-space wrap>
        <a-input v-model="keyword" placeholder="搜索角色（名称/编码/描述）" allow-clear style="width: 280px">
          <template #prefix>
            <icon-search />
          </template>
        </a-input>
        <a-select v-model="statusFilter" placeholder="状态" allow-clear style="width: 160px">
          <a-option value="启用">启用</a-option>
          <a-option value="禁用">禁用</a-option>
        </a-select>
        <a-date-picker v-model="dateRange" type="daterange" placeholder="创建时间范围" style="width: 300px" />
        <a-button type="primary" @click="applyFilters"><icon-search />筛选</a-button>
        <a-button @click="resetFilters"><icon-refresh />重置</a-button>
      </a-space>
    </a-card>

    <a-card class="table-card" :bordered="false">
      <div class="table-toolbar">
        <a-space>
          <a-button :disabled="selectedKeys.length === 0" @click="batchEnable">批量启用</a-button>
          <a-button :disabled="selectedKeys.length === 0" @click="batchDisable">批量禁用</a-button>
          <a-button status="danger" :disabled="selectedKeys.length === 0" @click="batchDelete">批量删除</a-button>
        </a-space>
      </div>
      <a-table
        row-key="id"
        :columns="columns"
        :data="roles"
        :row-selection="rowSelection"
        :pagination="{ pageSize: 10 }"
        :loading="loading"
      >
        <template #status="{ record }">
          <a-tag :color="record.status === '启用' ? 'green' : 'gray'">{{ record.status }}</a-tag>
        </template>
        <template #permissions="{ record }">
          <a-space wrap>
            <a-tag v-for="p in record.permissions" :key="p" color="arcoblue">{{ permissionLabel(p) }}</a-tag>
          </a-space>
        </template>
        <template #actions="{ record }">
          <a-space>
            <a-button size="small" @click="openEdit(record)"><icon-edit />编辑</a-button>
            <a-button size="small" status="danger" @click="deleteRole(record)"><icon-delete />删除</a-button>
          </a-space>
        </template>
      </a-table>
    </a-card>

    <a-modal v-model:visible="modalVisible" :title="isEdit ? '编辑角色' : '新增角色'" @ok="submit" @cancel="closeModal">
      <a-form :model="form" layout="vertical">
        <a-form-item field="name" label="角色名称" :rules="[{ required: true, message: '请输入角色名称' }]">
          <a-input v-model="form.name" />
        </a-form-item>
        <a-form-item field="code" label="角色编码" :rules="[{ required: true, message: '请输入角色编码' }]">
          <a-input v-model="form.code" />
        </a-form-item>
        <a-form-item field="description" label="描述">
          <a-textarea v-model="form.description" :max-length="200" show-word-limit />
        </a-form-item>
        <a-form-item field="status" label="状态" :rules="[{ required: true, message: '请选择状态' }]">
          <a-radio-group v-model="form.status">
            <a-radio value="启用">启用</a-radio>
            <a-radio value="禁用">禁用</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item field="permissions" label="权限配置" :rules="[{ required: true, message: '请至少选择一个权限' }]">
          <a-checkbox-group v-model="form.permissions">
            <a-space direction="vertical">
              <div v-for="group in permissionGroups" :key="group.key" class="perm-group">
                <div class="perm-group-title">{{ group.label }}</div>
                <a-space wrap>
                  <a-checkbox v-for="perm in group.items" :key="perm.key" :value="perm.key">
                    {{ perm.label }}
                  </a-checkbox>
                </a-space>
              </div>
            </a-space>
          </a-checkbox-group>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { Message } from '@arco-design/web-vue';
import { IconSearch, IconRefresh, IconPlus, IconEdit, IconDelete } from '@arco-design/web-vue/es/icon';
import type { RoleDTO } from '../../../api/roles';
import { listRoles, createRole, updateRoleApi, deleteRoleApi, batchUpdateRoleStatus, batchDeleteRoles } from '../../../api/roles';

// 类型定义
type RoleRow = {
  id: number;
  name: string;
  code: string;
  description?: string;
  status: '启用' | '禁用';
  permissions: string[];
  createdAt: string; // YYYY-MM-DD HH:mm
};

// 权限定义
const permissionGroups = [
  {
    key: 'device',
    label: '设备管理',
    items: [
      { key: 'device:view', label: '查看设备' },
      { key: 'device:edit', label: '编辑设备' }
    ]
  },
  {
    key: 'task',
    label: '任务管理',
    items: [
      { key: 'task:view', label: '查看任务' },
      { key: 'task:assign', label: '分配任务' }
    ]
  },
  {
    key: 'alert',
    label: '告警管理',
    items: [
      { key: 'alert:view', label: '查看告警' },
      { key: 'alert:handle', label: '处理告警' },
      { key: 'alert:rule', label: '规则管理' }
    ]
  },
  {
    key: 'graph',
    label: '知识图谱',
    items: [
      { key: 'graph:view', label: '图谱查看' },
      { key: 'graph:analysis', label: '图谱分析' }
    ]
  },
  {
    key: 'user',
    label: '用户管理',
    items: [
      { key: 'user:view', label: '查看用户' },
      { key: 'user:edit', label: '编辑用户' }
    ]
  }
];

const permissionLabel = (key: string) => {
  for (const g of permissionGroups) {
    const found = g.items.find(i => i.key === key);
    if (found) return found.label;
  }
  return key;
};

// 过滤器状态
const keyword = ref('');
const statusFilter = ref<RoleRow['status'] | undefined>();
const dateRange = ref<string[] | undefined>();

// 加载与数据
const loading = ref(false);
const roles = ref<RoleRow[]>([]);

const mapToRow = (r: RoleDTO): RoleRow => ({
  id: r.id,
  name: r.name,
  code: r.code,
  description: r.description || '',
  status: (r.status as RoleRow['status']) || '启用',
  permissions: (() => {
    try { return r.permissions ? JSON.parse(r.permissions as any) : []; } catch { return []; }
  })(),
  createdAt: r.createdAt || ''
});

const fetchRoles = async () => {
  loading.value = true;
  try {
    const params: any = {};
    if (keyword.value) params.keyword = keyword.value;
    if (statusFilter.value) params.status = statusFilter.value;
    if (dateRange.value && dateRange.value.length === 2) {
      const [start, end] = dateRange.value;
      if (start) params.start = new Date(start).toISOString();
      if (end) params.end = new Date(end).toISOString();
    }
    const resp = await listRoles(params);
    const list = (resp.data as unknown as RoleDTO[]) || [];
    roles.value = list.map(mapToRow);
  } catch (e: any) {
    Message.error(e?.message || '加载角色列表失败');
  } finally {
    loading.value = false;
  }
};

// 表格列
const columns = [
  { title: '角色名称', dataIndex: 'name', width: 160 },
  { title: '角色编码', dataIndex: 'code', width: 140 },
  { title: '描述', dataIndex: 'description' },
  { title: '状态', dataIndex: 'status', slotName: 'status', width: 100 },
  { title: '权限', dataIndex: 'permissions', slotName: 'permissions' },
  { title: '创建时间', dataIndex: 'createdAt', width: 160 },
  { title: '操作', slotName: 'actions', width: 180 }
];

// 表格选择
const selectedKeys = ref<number[]>([]);
const rowSelection = computed(() => ({
  type: 'checkbox',
  selectedRowKeys: selectedKeys.value,
  onChange: (keys: number[]) => { selectedKeys.value = keys; }
}));

// 模态与表单
const modalVisible = ref(false);
const isEdit = ref(false);
const form = reactive<{ id?: number; name: string; code: string; description?: string; status: RoleRow['status']; permissions: string[] }>({
  name: '', code: '', description: '', status: '启用', permissions: []
});

const openCreate = () => {
  isEdit.value = false;
  form.id = undefined;
  form.name = '';
  form.code = '';
  form.description = '';
  form.status = '启用';
  form.permissions = [];
  modalVisible.value = true;
};

const openEdit = (row: RoleRow) => {
  isEdit.value = true;
  form.id = row.id;
  form.name = row.name;
  form.code = row.code;
  form.description = row.description;
  form.status = row.status;
  form.permissions = [...row.permissions];
  modalVisible.value = true;
};

const closeModal = () => { modalVisible.value = false; };

const submit = async () => {
  if (!form.name || !form.code || !form.status) {
    Message.error('请填写完整信息');
    return;
  }
  try {
    const payload: Partial<RoleDTO> = {
      name: form.name,
      code: form.code,
      description: form.description,
      status: form.status,
      permissions: JSON.stringify(form.permissions)
    };
    if (isEdit.value && form.id) {
      await updateRoleApi(form.id, payload);
      Message.success('角色信息已更新');
    } else {
      await createRole(payload);
      Message.success('角色已创建');
    }
    modalVisible.value = false;
    await fetchRoles();
  } catch (e: any) {
    Message.error(e?.message || '提交失败');
  }
};

const deleteRole = async (row: RoleRow) => {
  try {
    await deleteRoleApi(row.id);
    Message.success(`角色 ${row.name} 已删除`);
    await fetchRoles();
  } catch (e: any) {
    Message.error(e?.message || '删除失败');
  }
};

const batchEnable = async () => {
  try {
    await batchUpdateRoleStatus(selectedKeys.value, '启用');
    Message.success('已批量启用选中角色');
    await fetchRoles();
  } catch (e: any) {
    Message.error(e?.message || '批量启用失败');
  }
};
const batchDisable = async () => {
  try {
    await batchUpdateRoleStatus(selectedKeys.value, '禁用');
    Message.success('已批量禁用选中角色');
    await fetchRoles();
  } catch (e: any) {
    Message.error(e?.message || '批量禁用失败');
  }
};
const batchDelete = async () => {
  try {
    await batchDeleteRoles(selectedKeys.value);
    selectedKeys.value = [];
    Message.success('已批量删除选中角色');
    await fetchRoles();
  } catch (e: any) {
    Message.error(e?.message || '批量删除失败');
  }
};

const applyFilters = async () => { await fetchRoles(); };
const resetFilters = async () => { keyword.value = ''; statusFilter.value = undefined; dateRange.value = undefined; await fetchRoles(); };
const refresh = async () => { await fetchRoles(); };

onMounted(fetchRoles);
</script>

<style scoped>
.page-container { padding: 16px; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.page-title { font-size: 18px; font-weight: 600; margin: 8px 0; }
.filters-card { margin-bottom: 12px; }
.table-toolbar { margin-bottom: 12px; display: flex; justify-content: space-between; }
.perm-group { margin-bottom: 12px; }
.perm-group-title { font-weight: 600; margin-bottom: 6px; }
</style>