<template>
  <div class="page-container">
    <div class="page-header">
      <a-breadcrumb>
        <a-breadcrumb-item>任务管理</a-breadcrumb-item>
        <a-breadcrumb-item>任务列表</a-breadcrumb-item>
      </a-breadcrumb>
      <h1 class="page-title">任务列表</h1>
      <div class="header-actions">
        <a-button type="primary" @click="openCreate">
          <icon-plus />
          新建任务
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
          <a-input-search v-model="keyword" placeholder="搜索任务名称/设备/巡检员" allow-clear @search="applyFilters">
            <template #prefix><icon-search /></template>
          </a-input-search>
        </a-col>
        <a-col :xs="12" :sm="6" :md="4">
          <a-select v-model="statusFilter" placeholder="任务状态" allow-clear @change="applyFilters">
            <a-option value="待分配">待分配</a-option>
            <a-option value="进行中">进行中</a-option>
            <a-option value="已完成">已完成</a-option>
            <a-option value="已取消">已取消</a-option>
          </a-select>
        </a-col>
        <a-col :xs="12" :sm="6" :md="4">
          <a-select v-model="priorityFilter" placeholder="优先级" allow-clear @change="applyFilters">
            <a-option value="低">低</a-option>
            <a-option value="中">中</a-option>
            <a-option value="高">高</a-option>
          </a-select>
        </a-col>
        <a-col :xs="12" :sm="6" :md="4">
          <a-select v-model="assigneeFilter" placeholder="巡检员" allow-clear @change="applyFilters">
            <a-option v-for="u in users" :key="u" :value="u">{{ u }}</a-option>
          </a-select>
        </a-col>
      </a-row>
    </a-card>

    <a-card class="table-card" :bordered="false">
      <a-table :columns="columns" :data="pagedData" :pagination="pagination" row-key="id">
        <template #status="{ record }">
          <a-tag :color="statusColor(record.status)">{{ record.status }}</a-tag>
        </template>
        <template #priority="{ record }">
          <a-tag :color="priorityColor(record.priority)">{{ record.priority }}</a-tag>
        </template>
        <template #actions="{ record }">
          <a-space>
            <a-button type="text" size="small" @click="viewTask(record)">查看</a-button>
            <a-button type="text" size="small" @click="editTask(record)">编辑</a-button>
            <a-button type="text" size="small" @click="markDone(record)">标记完成</a-button>
            <a-popconfirm content="确认删除该任务？" @ok="removeTask(record.id)">
              <a-button type="text" size="small" status="danger">删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </a-table>
    </a-card>

    <a-modal v-model:visible="editVisible" :title="editing ? '编辑任务' : '新建任务'" @ok="saveTask" @cancel="closeEdit">
      <a-form :model="editForm" layout="vertical">
        <a-form-item label="任务名称" field="title" :rules="[{ required: true, message: '请输入任务名称' }]">
          <a-input v-model="editForm.title" placeholder="请输入任务名称" />
        </a-form-item>
        <a-form-item label="关联设备" field="device" :rules="[{ required: true, message: '请输入关联设备' }]">
          <a-input v-model="editForm.device" placeholder="如：主变压器 A" />
        </a-form-item>
        <a-form-item label="巡检员" field="assignee" :rules="[{ required: true, message: '请选择巡检员' }]">
          <a-select v-model="editForm.assignee" placeholder="请选择">
            <a-option v-for="u in users" :key="u" :value="u">{{ u }}</a-option>
          </a-select>
        </a-form-item>
        <a-form-item label="优先级" field="priority" :rules="[{ required: true, message: '请选择优先级' }]">
          <a-select v-model="editForm.priority" placeholder="请选择">
            <a-option value="低">低</a-option>
            <a-option value="中">中</a-option>
            <a-option value="高">高</a-option>
          </a-select>
        </a-form-item>
        <a-form-item label="截止日期" field="dueDate" :rules="[{ required: true, message: '请选择截止日期' }]">
          <a-date-picker v-model="editForm.dueDate" style="width: 100%" />
        </a-form-item>
        <a-form-item label="状态" field="status" :rules="[{ required: true, message: '请选择状态' }]">
          <a-select v-model="editForm.status" placeholder="请选择">
            <a-option value="待分配">待分配</a-option>
            <a-option value="进行中">进行中</a-option>
            <a-option value="已完成">已完成</a-option>
            <a-option value="已取消">已取消</a-option>
          </a-select>
        </a-form-item>
        <a-form-item label="任务说明" field="description">
          <a-textarea v-model="editForm.description" placeholder="补充任务说明" :auto-size="{ minRows: 3, maxRows: 6 }" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue';
import { Message } from '@arco-design/web-vue';
import { IconPlus, IconRefresh, IconSearch } from '@arco-design/web-vue/es/icon';

type Task = {
  id: number;
  title: string;
  device: string;
  assignee: string;
  status: '待分配' | '进行中' | '已完成' | '已取消';
  priority: '低' | '中' | '高';
  dueDate: string;
  description?: string;
};

const users = ref<string[]>(['张三','李四','王五','赵六']);

const tasks = ref<Task[]>([
  { id: 1, title: '主变压器温度巡检', device: '主变压器 A', assignee: '张三', status: '进行中', priority: '高', dueDate: '2025-10-10', description: '检查油温与绕组温度' },
  { id: 2, title: '断路器开关测试', device: '高压断路器 C', assignee: '李四', status: '待分配', priority: '中', dueDate: '2025-10-12' },
  { id: 3, title: '环网柜外观检查', device: '环网柜 D', assignee: '王五', status: '已完成', priority: '低', dueDate: '2025-10-08' },
  { id: 4, title: '传感器数据校准', device: '温度传感器 E', assignee: '赵六', status: '进行中', priority: '中', dueDate: '2025-10-11' }
]);

const keyword = ref('');
const statusFilter = ref<string | undefined>();
const priorityFilter = ref<string | undefined>();
const assigneeFilter = ref<string | undefined>();

const filteredData = computed(() => {
  return tasks.value.filter(t => {
    const kw = keyword.value.trim().toLowerCase();
    const kwMatch = !kw || [t.title, t.device, t.assignee].some(v => v.toLowerCase().includes(kw));
    const statusMatch = !statusFilter.value || t.status === statusFilter.value;
    const priorityMatch = !priorityFilter.value || t.priority === priorityFilter.value;
    const assigneeMatch = !assigneeFilter.value || t.assignee === assigneeFilter.value;
    return kwMatch && statusMatch && priorityMatch && assigneeMatch;
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
  { title: '任务名称', dataIndex: 'title' },
  { title: '关联设备', dataIndex: 'device' },
  { title: '巡检员', dataIndex: 'assignee' },
  { title: '状态', slotName: 'status', width: 120 },
  { title: '优先级', slotName: 'priority', width: 120 },
  { title: '截止日期', dataIndex: 'dueDate', width: 140 },
  { title: '操作', slotName: 'actions', width: 220 }
];

const statusColor = (s: Task['status']) => {
  if (s === '进行中') return 'arcoblue';
  if (s === '待分配') return 'orange';
  if (s === '已完成') return 'green';
  return 'red';
};

const priorityColor = (p: Task['priority']) => {
  if (p === '高') return 'red';
  if (p === '中') return 'orange';
  return 'green';
};

const editVisible = ref(false);
const editing = ref(false);
const editForm = reactive<Task>({ id: 0, title: '', device: '', assignee: '', status: '待分配', priority: '中', dueDate: '', description: '' });

const applyFilters = () => { pagination.current = 1; };
const refresh = () => { Message.success('列表已刷新'); };

const openCreate = () => {
  editing.value = false;
  Object.assign(editForm, { id: 0, title: '', device: '', assignee: '', status: '待分配', priority: '中', dueDate: '', description: '' });
  editVisible.value = true;
};

const viewTask = (record: Task) => { Message.info(`查看任务：${record.title}`); };

const editTask = (record: Task) => {
  editing.value = true;
  Object.assign(editForm, record);
  editVisible.value = true;
};

const closeEdit = () => { editVisible.value = false; };

const saveTask = () => {
  if (!editForm.title || !editForm.device || !editForm.assignee || !editForm.dueDate) {
    Message.error('请完善任务信息');
    return;
  }
  if (editing.value) {
    const idx = tasks.value.findIndex(t => t.id === editForm.id);
    if (idx >= 0) {
      tasks.value[idx] = { ...editForm };
      Message.success('任务已更新');
    }
  } else {
    const newId = Math.max(...tasks.value.map(t => t.id)) + 1;
    tasks.value.push({ ...editForm, id: newId });
    Message.success('任务已创建');
  }
  editVisible.value = false;
};

const markDone = (record: Task) => {
  const idx = tasks.value.findIndex(t => t.id === record.id);
  if (idx >= 0) {
    if (idx >= 0 && tasks.value[idx]) {
      tasks.value[idx].status = '已完成';
    }
    Message.success('已标记为完成');
  }
};

const removeTask = (id: number) => {
  tasks.value = tasks.value.filter(t => t.id !== id);
  Message.success('任务已删除');
};
</script>

<style scoped>
.page-container { padding: 16px; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.page-title { font-size: 18px; font-weight: 600; margin: 8px 0; }
.header-actions { display: flex; gap: 8px; }
.filters-card { margin-bottom: 12px; }
.table-card { }
</style>