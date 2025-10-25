<template>
  <div class="page-container">
    <div class="page-header">
      <a-breadcrumb>
        <a-breadcrumb-item>用户管理</a-breadcrumb-item>
        <a-breadcrumb-item>部门管理</a-breadcrumb-item>
      </a-breadcrumb>
      <h1 class="page-title">部门管理</h1>
      <div class="header-actions">
        <a-button type="primary" @click="openCreate"><icon-plus />新增部门</a-button>
        <a-button @click="refresh"><icon-refresh />刷新</a-button>
      </div>
    </div>

    <a-card class="main-card" :bordered="false">
      <a-row :gutter="12">
        <a-col :span="8">
          <a-card title="部门树" :bordered="false">
            <a-input v-model="treeKeyword" placeholder="搜索部门" allow-clear style="margin-bottom: 8px">
              <template #prefix><icon-search /></template>
            </a-input>
            <a-tree
              :data="treeData"
              :selected-keys="selectedDeptId ? [selectedDeptId] : []"
              block-node
              @select="onTreeSelect"
            />
          </a-card>
        </a-col>
        <a-col :span="16">
          <a-card :bordered="false" title="部门列表">
            <div class="table-toolbar">
              <a-space>
                <a-button :disabled="selectedKeys.length===0" @click="batchEnable">批量启用</a-button>
                <a-button :disabled="selectedKeys.length===0" @click="batchDisable">批量禁用</a-button>
                <a-button status="danger" :disabled="selectedKeys.length===0" @click="batchDelete">批量删除</a-button>
              </a-space>
              <a-space>
                <a-input v-model="keyword" placeholder="搜索（名称/负责人）" allow-clear style="width: 260px">
                  <template #prefix><icon-search /></template>
                </a-input>
                <a-select v-model="statusFilter" placeholder="状态" allow-clear style="width: 160px">
                  <a-option value="启用">启用</a-option>
                  <a-option value="禁用">禁用</a-option>
                </a-select>
                <a-button type="primary" @click="applyFilters"><icon-search />筛选</a-button>
                <!-- <a-button @click="resetFilters"><icon-refresh />重置</a-button> -->
              </a-space>
            </div>
            <a-table
              row-key="id"
              :columns="columns"
              :data="rows"
              :row-selection="rowSelection"
              :pagination="{ pageSize: 10 }"
              :loading="loading"
            >
              <template #status="{ record }">
                <a-tag :color="record.status === '启用' ? 'green' : 'gray'">{{ record.status }}</a-tag>
              </template>
              <template #actions="{ record }">
                <a-space>
                  <a-button size="small" @click="openEdit(record)"><icon-edit />编辑</a-button>
                  <a-button size="small" status="danger" @click="deleteDept(record)"><icon-delete />删除</a-button>
                </a-space>
              </template>
            </a-table>
          </a-card>
        </a-col>
      </a-row>
    </a-card>

    <a-modal v-model:visible="modalVisible" :title="isEdit ? '编辑部门' : '新增部门'" @ok="submit" @cancel="closeModal">
      <a-form :model="form" layout="vertical">
        <a-form-item field="name" label="部门名称" :rules="[{ required: true, message: '请输入部门名称' }]">
          <a-input v-model="form.name" />
        </a-form-item>
        <a-form-item field="leader" label="负责人">
          <a-input v-model="form.leader" />
        </a-form-item>
        <a-form-item field="parentId" label="上级部门">
          <a-tree-select v-model="form.parentId" :data="treeData" allow-clear placeholder="选择上级部门" />
        </a-form-item>
        <a-form-item field="status" label="状态" :rules="[{ required: true, message: '请选择状态' }]">
          <a-radio-group v-model="form.status">
            <a-radio value="启用">启用</a-radio>
            <a-radio value="禁用">禁用</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item field="description" label="描述">
          <a-textarea v-model="form.description" :max-length="200" show-word-limit />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue';
import { Message } from '@arco-design/web-vue';
import { IconSearch, IconRefresh, IconPlus, IconEdit, IconDelete } from '@arco-design/web-vue/es/icon';
import { getDeptTree, listDepts, createDept, updateDeptApi, deleteDeptApi, batchUpdateDeptStatus, batchDeleteDepts } from '../../../api/departments';
import type { DeptDTO, TreeNode } from '../../../api/departments';

// 类型定义
type DeptRow = {
  id: number;
  name: string;
  parentId?: number;
  leader?: string;
  status: '启用' | '禁用';
  description?: string;
};

// 部门树数据
const rawTree = ref<TreeNode[]>([]);
const treeKeyword = ref('');
const treeData = computed(() => {
  if (!treeKeyword.value) return rawTree.value;
  const filterTree = (nodes: any[]): any[] => nodes
    .map(n => ({ ...n, children: n.children ? filterTree(n.children) : undefined }))
    .filter(n => n.title.includes(treeKeyword.value) || (n.children && n.children.length));
  return filterTree(rawTree.value);
});

// 列表与过滤
const keyword = ref('');
const statusFilter = ref<DeptRow['status'] | undefined>();
const selectedDeptId = ref<number | undefined>(undefined);

const loading = ref(false);
const rows = ref<DeptRow[]>([]);

const columns = [
  { title: '部门名称', dataIndex: 'name', width: 180 },
  { title: '上级部门', dataIndex: 'parentId', render: ({ record }: { record: DeptRow }) => parentName(record.parentId) },
  { title: '负责人', dataIndex: 'leader', width: 140 },
  { title: '状态', dataIndex: 'status', slotName: 'status', width: 100 },
  { title: '描述', dataIndex: 'description' },
  { title: '操作', slotName: 'actions', width: 180 }
];

const parentName = (pid?: number) => {
  if (!pid) return '-';
  const p = rows.value.find(r => r.id === pid);
  return p ? p.name : '-';
};

const selectedKeys = ref<number[]>([]);
const rowSelection = computed(() => ({
  type: 'checkbox',
  selectedRowKeys: selectedKeys.value,
  onChange: (keys: number[]) => { selectedKeys.value = keys; }
}));

const fetchTree = async () => {
  try {
    const resp = await getDeptTree();
    rawTree.value = (resp.data as unknown as TreeNode[]) || [];
  } catch (e: any) {
    Message.error(e?.message || '加载部门树失败');
  }
};

const fetchRows = async () => {
  loading.value = true;
  try {
    const params: any = {};
    if (keyword.value) params.keyword = keyword.value;
    if (statusFilter.value) params.status = statusFilter.value;
    if (selectedDeptId.value) params.parentId = selectedDeptId.value;
    const resp = await listDepts(params);
    rows.value = (resp.data as unknown as DeptDTO[]) || [];
  } catch (e: any) {
    Message.error(e?.message || '加载部门列表失败');
  } finally {
    loading.value = false;
  }
};

const onTreeSelect = (keys: (string | number)[]) => {
  const k = keys[0];
  selectedDeptId.value = typeof k === 'number' ? k : Number(k);
};

// 模态与表单
const modalVisible = ref(false);
const isEdit = ref(false);
const form = reactive<{ id?: number; name: string; parentId?: number; leader?: string; status: DeptRow['status']; description?: string }>({
  name: '', parentId: undefined, leader: '', status: '启用', description: ''
});

const openCreate = () => {
  isEdit.value = false;
  form.id = undefined;
  form.name = '';
  form.parentId = selectedDeptId.value;
  form.leader = '';
  form.status = '启用';
  form.description = '';
  modalVisible.value = true;
};

const openEdit = (row: DeptRow) => {
  isEdit.value = true;
  form.id = row.id;
  form.name = row.name;
  form.parentId = row.parentId;
  form.leader = row.leader;
  form.status = row.status;
  form.description = row.description;
  modalVisible.value = true;
};

const closeModal = () => { modalVisible.value = false; };

const submit = async () => {
  if (!form.name || !form.status) {
    Message.error('请填写完整信息');
    return;
  }
  try {
    const payload: Partial<DeptDTO> = {
      name: form.name,
      parentId: form.parentId,
      leader: form.leader,
      status: form.status,
      description: form.description
    };
    if (isEdit.value && form.id) {
      await updateDeptApi(form.id, payload);
      Message.success('部门信息已更新');
    } else {
      await createDept(payload);
      Message.success('部门已创建');
    }
    modalVisible.value = false;
    await fetchRows();
    await fetchTree();
  } catch (e: any) {
    Message.error(e?.message || '提交失败');
  }
};

const deleteDept = async (row: DeptRow) => {
  try {
    await deleteDeptApi(row.id);
    Message.success(`部门 ${row.name} 已删除`);
    await fetchRows();
    await fetchTree();
  } catch (e: any) {
    Message.error(e?.message || '删除失败');
  }
};

const batchEnable = async () => {
  try {
    await batchUpdateDeptStatus(selectedKeys.value, '启用');
    Message.success('已批量启用选中部门');
    await fetchRows();
  } catch (e: any) {
    Message.error(e?.message || '批量启用失败');
  }
};
const batchDisable = async () => {
  try {
    await batchUpdateDeptStatus(selectedKeys.value, '禁用');
    Message.success('已批量禁用选中部门');
    await fetchRows();
  } catch (e: any) {
    Message.error(e?.message || '批量禁用失败');
  }
};
const batchDelete = async () => {
  try {
    await batchDeleteDepts(selectedKeys.value);
    selectedKeys.value = [];
    Message.success('已批量删除选中部门');
    await fetchRows();
    await fetchTree();
  } catch (e: any) {
    Message.error(e?.message || '批量删除失败');
  }
};

const applyFilters = async () => { await fetchRows(); };
const resetFilters = async () => { keyword.value = ''; statusFilter.value = undefined; await fetchRows(); };
const refresh = async () => { await fetchRows(); await fetchTree(); };

onMounted(async () => { await fetchTree(); await fetchRows(); });
</script>

<style scoped>
.page-container { padding: 16px; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.page-title { font-size: 18px; font-weight: 600; margin: 8px 0; }
.table-toolbar { margin-bottom: 12px; display: flex; justify-content: space-between; }
.main-card { }
</style>