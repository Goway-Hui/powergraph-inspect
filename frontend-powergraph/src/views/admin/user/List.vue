<template>
  <div class="page-container">
    <div class="page-header">
      <a-breadcrumb>
        <a-breadcrumb-item>用户管理</a-breadcrumb-item>
        <a-breadcrumb-item>用户列表</a-breadcrumb-item>
      </a-breadcrumb>
      <h1 class="page-title">用户列表</h1>
      <div class="header-actions">
        <a-button type="primary" @click="openCreate"><icon-plus />新增用户</a-button>
        <a-button @click="refresh"><icon-refresh />刷新</a-button>
      </div>
    </div>

    <a-card class="filters-card" :bordered="false">
      <a-space wrap>
        <a-input v-model="keyword" placeholder="搜索关键词（姓名/邮箱/手机号）" allow-clear style="width: 260px">
          <template #prefix>
            <icon-search />
          </template>
        </a-input>
        <a-select v-model="roleFilter" placeholder="角色" allow-clear style="width: 160px">
          <a-option value="ADMIN">管理员</a-option>
          <a-option value="INSPECTOR">巡检员</a-option>
          <a-option value="VIEWER">观察员</a-option>
        </a-select>
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
        :data="filteredRows"
        :row-selection="rowSelection"
        :pagination="{ pageSize: 10 }"
        :loading="loading"
      >
        <template #role="{ record }">
          <a-tag :color="roleColor(record.role)">{{ roleLabel(record.role) }}</a-tag>
        </template>
        <template #status="{ record }">
          <a-tag :color="record.status === '启用' ? 'green' : 'gray'">{{ record.status }}</a-tag>
        </template>
        <template #actions="{ record }">
          <a-space>
            <a-button size="small" @click="openEdit(record)"><icon-edit />编辑</a-button>
            <a-button size="small" status="danger" @click="deleteUser(record)"><icon-delete />删除</a-button>
          </a-space>
        </template>
      </a-table>
    </a-card>

    <a-modal v-model:visible="modalVisible" :title="isEdit ? '编辑用户' : '新增用户'" @ok="submit" @cancel="closeModal">
      <a-form :model="form" layout="vertical">
        <a-form-item field="name" label="姓名" :rules="[{ required: true, message: '请输入姓名' }]">
          <a-input v-model="form.name" placeholder="请输入姓名" />
        </a-form-item>
        <a-form-item field="username" label="用户名" :rules="[{ required: true, message: '请输入用户名' }]">
          <a-input v-model="form.username" placeholder="请输入用户名" />
        </a-form-item>
        <a-form-item v-if="!isEdit" field="password" label="密码" :rules="[{ required: true, message: '请输入密码' }]">
          <a-input-password v-model="form.password" placeholder="请输入密码" />
        </a-form-item>
        <a-form-item field="role" label="角色" :rules="[{ required: true, message: '请选择角色' }]">
          <a-select v-model="form.role" placeholder="请选择角色">
            <a-option value="ADMIN">管理员</a-option>
            <a-option value="INSPECTOR">巡检员</a-option>
            <a-option value="VIEWER">观察员</a-option>
          </a-select>
        </a-form-item>
        <a-form-item field="department" label="部门">
          <a-input v-model="form.department" placeholder="请输入部门" />
        </a-form-item>
        <a-form-item field="email" label="邮箱" :rules="[{ type: 'email', message: '邮箱格式不正确' }]">
          <a-input v-model="form.email" placeholder="请输入邮箱" />
        </a-form-item>
        <a-form-item field="phone" label="手机号">
          <a-input v-model="form.phone" placeholder="请输入手机号" />
        </a-form-item>
        <a-form-item field="status" label="状态" :rules="[{ required: true, message: '请选择状态' }]">
          <a-radio-group v-model="form.status">
            <a-radio value="启用">启用</a-radio>
            <a-radio value="禁用">禁用</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { Message } from '@arco-design/web-vue';
import { IconSearch, IconRefresh, IconPlus, IconEdit, IconDelete } from '@arco-design/web-vue/es/icon';
import { listUsers, createUser, updateUser as updateUserApi, deleteUserApi, batchUpdateStatus, batchDelete as batchDeleteApi } from '../../../api/users';
import type { UserDTO } from '../../../api/users';

// 类型定义
type UserRow = {
  id: number;
  name: string;
  username: string;
  role: 'ADMIN' | 'INSPECTOR' | 'VIEWER';
  department?: string;
  email?: string;
  phone?: string;
  status: '启用' | '禁用';
  createdAt: string; // YYYY-MM-DD HH:mm
};

// 过滤器状态
const keyword = ref('');
const roleFilter = ref<UserRow['role'] | undefined>();
const statusFilter = ref<UserRow['status'] | undefined>();
const dateRange = ref<string[] | undefined>();

// 数据
const users = ref<UserRow[]>([]);
const loading = ref(false);

const mapToRow = (u: UserDTO): UserRow => ({
  id: Number(u.id || 0),
  name: u.name,
  username: u.username,
  role: u.role,
  status: (u.status as UserRow['status']) || '启用',
  createdAt: (u.createdAt || '').replace('T', ' ').slice(0, 16)
});

const fetchUsers = async () => {
  try {
    loading.value = true;
    const params: any = {};
    if (keyword.value) params.keyword = keyword.value;
    if (roleFilter.value) params.role = roleFilter.value;
    if (statusFilter.value) params.status = statusFilter.value;
    if (dateRange.value && dateRange.value.length === 2) {
      const [start, end] = dateRange.value;
      if (start) params.start = new Date(start).toISOString();
      if (end) params.end = new Date(end).toISOString();
    }
    const resp = await listUsers(params);
    users.value = (resp.data || []).map(mapToRow);
  } catch (e: any) {
    Message.error(e.message || '获取用户失败');
  } finally {
    loading.value = false;
  }
};

onMounted(fetchUsers);

// 表格列
const columns = [
  { title: 'ID', dataIndex: 'id', width: 80 },
  { title: '姓名', dataIndex: 'name', width: 120 },
  { title: '用户名', dataIndex: 'username', width: 140 },
  { title: '角色', dataIndex: 'role', slotName: 'role', width: 120 },
  { title: '部门', dataIndex: 'department', width: 140 },
  { title: '状态', dataIndex: 'status', slotName: 'status', width: 100 },
  { title: '邮箱', dataIndex: 'email', width: 200 },
  { title: '手机号', dataIndex: 'phone', width: 140 },
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

// 过滤逻辑
const filteredRows = computed(() => users.value);

// 标签颜色与文案
const roleLabel = (role: UserRow['role']) => {
  return role === 'ADMIN' ? '管理员' : role === 'INSPECTOR' ? '巡检员' : '观察员';
};
const roleColor = (role: UserRow['role']) => {
  return role === 'ADMIN' ? 'arcoblue' : role === 'INSPECTOR' ? 'green' : 'orange';
};

// 模态与表单
const modalVisible = ref(false);
const isEdit = ref(false);
const form = reactive<{ id?: number; name: string; username: string; password?: string; role: UserRow['role']; department?: string; email?: string; phone?: string; status: UserRow['status']; }>({
  name: '', username: '', role: 'VIEWER', status: '启用', department: '', email: '', phone: ''
});

const openCreate = () => {
  isEdit.value = false;
  form.id = undefined;
  form.name = '';
  form.username = '';
  form.password = '';
  form.role = 'VIEWER';
  form.department = '';
  form.email = '';
  form.phone = '';
  form.status = '启用';
  modalVisible.value = true;
};

const openEdit = (row: UserRow) => {
  isEdit.value = true;
  form.id = row.id;
  form.name = row.name;
  form.username = row.username;
  form.password = undefined;
  form.role = row.role;
  form.department = row.department;
  form.email = row.email;
  form.phone = row.phone;
  form.status = row.status;
  modalVisible.value = true;
};

const closeModal = () => { modalVisible.value = false; };

const submit = async () => {
  if (!form.name || !form.username || !form.role || !form.status) {
    Message.error('请填写完整信息');
    return;
  }
  if (!isEdit.value && !form.password) {
    Message.error('新增用户需要设置密码');
    return;
  }

  try {
    if (isEdit.value && form.id) {
      const payload: UserDTO = {
        name: form.name,
        username: form.username,
        role: form.role,
        status: form.status
      };
      await updateUserApi(form.id, payload);
      Message.success('用户信息已更新');
    } else {
      const payload: UserDTO = {
        name: form.name,
        username: form.username,
        password: form.password!,
        role: form.role,
        status: form.status
      };
      await createUser(payload);
      Message.success('用户已创建');
    }
    modalVisible.value = false;
    await fetchUsers();
  } catch (e: any) {
    Message.error(e.message || '提交失败');
  }
};

// 单行删除
const deleteUser = async (row: UserRow) => {
  try {
    await deleteUserApi(row.id);
    Message.success(`用户 ${row.username} 已删除`);
    await fetchUsers();
  } catch (e: any) {
    Message.error(e.message || '删除失败');
  }
};

// 批量操作
const batchEnable = async () => {
  try {
    await batchUpdateStatus(selectedKeys.value, '启用');
    Message.success('已批量启用选中用户');
    selectedKeys.value = [];
    await fetchUsers();
  } catch (e: any) {
    Message.error(e.message || '批量启用失败');
  }
};
const batchDisable = async () => {
  try {
    await batchUpdateStatus(selectedKeys.value, '禁用');
    Message.success('已批量禁用选中用户');
    selectedKeys.value = [];
    await fetchUsers();
  } catch (e: any) {
    Message.error(e.message || '批量禁用失败');
  }
};
const batchDelete = async () => {
  try {
    await batchDeleteApi(selectedKeys.value);
    selectedKeys.value = [];
    Message.success('已批量删除选中用户');
    await fetchUsers();
  } catch (e: any) {
    Message.error(e.message || '批量删除失败');
  }
};

// 其他
const applyFilters = async () => { await fetchUsers(); };
const resetFilters = async () => { keyword.value = ''; roleFilter.value = undefined; statusFilter.value = undefined; dateRange.value = undefined; await fetchUsers(); };
const refresh = async () => { await fetchUsers(); };
</script>

<style scoped>
.page-container { padding: 16px; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.page-title { font-size: 18px; font-weight: 600; margin: 8px 0; }
.filters-card { margin-bottom: 12px; }
.table-card { }
.table-toolbar { margin-bottom: 12px; display: flex; justify-content: space-between; }
</style>