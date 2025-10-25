<template>
  <div class="page-container">
    <div class="page-header">
      <a-breadcrumb>
        <a-breadcrumb-item>设备管理</a-breadcrumb-item>
        <a-breadcrumb-item>设备分类</a-breadcrumb-item>
      </a-breadcrumb>
      <h1 class="page-title">设备分类</h1>
    </div>

    <a-card :bordered="false" class="toolbar-card">
      <a-space>
        <a-input v-model="newCategory" placeholder="输入新分类名称" style="width: 220px" />
        <a-button type="primary" @click="addCategory">
          <icon-plus />
          新增分类
        </a-button>
        <a-button @click="refresh"><icon-refresh />刷新</a-button>
      </a-space>
    </a-card>

    <a-card :bordered="false" class="table-card">
      <a-table :columns="columns" :data="categoryRows" row-key="name">
        <template #status="{ record }">
          <a-tag :color="record.count > 0 ? 'arcoblue' : 'green'">{{ record.count > 0 ? '使用中' : '空闲' }}</a-tag>
        </template>
        <template #actions="{ record }">
          <a-space>
            <a-button type="text" size="small" @click="renameCategory(record)">重命名</a-button>
            <a-popconfirm :content="record.count > 0 ? '该分类下有设备，无法删除' : '确认删除该分类？'" :ok-button-props="{ disabled: record.count > 0 }" @ok="removeCategory(record.name)">
              <a-button type="text" size="small" status="danger" :disabled="record.count > 0">删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </a-table>
    </a-card>

    <a-modal v-model:visible="renameVisible" title="重命名分类" @ok="confirmRename" @cancel="renameVisible = false">
      <a-form layout="vertical">
        <a-form-item label="新名称">
          <a-input v-model="renameTargetNew" placeholder="请输入新分类名称" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { Message } from '@arco-design/web-vue';
import { IconPlus, IconRefresh } from '@arco-design/web-vue/es/icon';

type CategoryRow = { name: string; count: number };

const categoryRows = ref<CategoryRow[]>([
  { name: '变压器', count: 12 },
  { name: '断路器', count: 8 },
  { name: '开关柜', count: 15 },
  { name: '电缆', count: 22 },
  { name: '传感器', count: 30 }
]);

const newCategory = ref('');

const columns = [
  { title: '分类名称', dataIndex: 'name' },
  { title: '设备数量', dataIndex: 'count', width: 120 },
  { title: '状态', slotName: 'status', width: 120 },
  { title: '操作', slotName: 'actions', width: 200 }
];

const addCategory = () => {
  const name = newCategory.value.trim();
  if (!name) { Message.error('请输入分类名称'); return; }
  if (categoryRows.value.some(c => c.name === name)) { Message.warning('分类已存在'); return; }
  categoryRows.value.push({ name, count: 0 });
  newCategory.value = '';
  Message.success('分类已新增');
};

const refresh = () => { Message.success('分类列表已刷新'); };

const renameVisible = ref(false);
const renameTargetOld = ref('');
const renameTargetNew = ref('');

const renameCategory = (row: CategoryRow) => {
  renameTargetOld.value = row.name;
  renameTargetNew.value = row.name;
  renameVisible.value = true;
};

const confirmRename = () => {
  const name = renameTargetNew.value.trim();
  if (!name) { Message.error('请输入新分类名称'); return; }
  if (categoryRows.value.some(c => c.name === name)) { Message.warning('分类已存在'); return; }
  const target = categoryRows.value.find(c => c.name === renameTargetOld.value);
  if (target) {
    target.name = name;
    Message.success('分类已重命名');
  }
  renameVisible.value = false;
};

const removeCategory = (name: string) => {
  const target = categoryRows.value.find(c => c.name === name);
  if (!target) return;
  if (target.count > 0) { Message.error('该分类下有设备，无法删除'); return; }
  categoryRows.value = categoryRows.value.filter(c => c.name !== name);
  Message.success('分类已删除');
};
</script>

<style scoped>
.page-container { padding: 16px; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.page-title { font-size: 18px; font-weight: 600; margin: 8px 0; }
.toolbar-card { margin-bottom: 12px; }
.table-card { }
</style>