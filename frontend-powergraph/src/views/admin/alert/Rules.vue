<template>
  <div class="page-container">
    <div class="page-header">
      <a-breadcrumb>
        <a-breadcrumb-item>告警管理</a-breadcrumb-item>
        <a-breadcrumb-item>告警规则</a-breadcrumb-item>
      </a-breadcrumb>
      <h1 class="page-title">告警规则</h1>
      <div class="header-actions">
        <a-button type="primary" @click="openCreate"><icon-plus />新增规则</a-button>
        <a-button @click="refresh"><icon-refresh />刷新</a-button>
      </div>
    </div>

    <a-card class="filters-card" :bordered="false">
      <a-space wrap>
        <a-input v-model="keyword" placeholder="搜索规则名称/条件" allow-clear style="width: 260px">
          <template #prefix>
            <icon-search />
          </template>
        </a-input>
        <a-select v-model="levelFilter" placeholder="级别" allow-clear style="width: 160px">
          <a-option value="低">低</a-option>
          <a-option value="中">中</a-option>
          <a-option value="高">高</a-option>
          <a-option value="严重">严重</a-option>
        </a-select>
        <a-select v-model="enabledFilter" placeholder="启用状态" allow-clear style="width: 160px">
          <a-option value="启用">启用</a-option>
          <a-option value="停用">停用</a-option>
        </a-select>
        <a-button type="primary" @click="applyFilters"><icon-search />筛选</a-button>
        <a-button @click="resetFilters"><icon-refresh />重置</a-button>
      </a-space>
    </a-card>

    <a-card class="table-card" :bordered="false">
      <a-table row-key="id" :columns="columns" :data="filteredRows" :pagination="{ pageSize: 10 }">
        <template #level="{ record }">
          <a-tag :color="levelColor(record.level)">{{ record.level }}</a-tag>
        </template>
        <template #enabled="{ record }">
          <a-switch v-model="record.enabled" @change="toggleEnable(record)"></a-switch>
        </template>
        <template #actions="{ record }">
          <a-space>
            <a-button size="small" @click="editRule(record)">编辑</a-button>
            <a-button size="small" status="danger" @click="deleteRuleAction(record)">删除</a-button>
          </a-space>
        </template>
      </a-table>
    </a-card>

    <a-modal v-model:visible="editVisible" :title="editMode === 'create' ? '新增规则' : '编辑规则'" @ok="saveRule" @cancel="closeEdit">
      <a-form :model="form" layout="vertical">
        <a-form-item field="name" label="规则名称" required>
          <a-input v-model="form.name" placeholder="例如：主变温度过高" />
        </a-form-item>
        <a-form-item field="level" label="告警级别" required>
          <a-select v-model="form.level" placeholder="选择告警级别">
            <a-option value="低">低</a-option>
            <a-option value="中">中</a-option>
            <a-option value="高">高</a-option>
            <a-option value="严重">严重</a-option>
          </a-select>
        </a-form-item>
        <a-form-item field="condition" label="触发条件" required>
          <a-input v-model="form.condition" placeholder="例如：温度 > 85℃，或表达式" />
        </a-form-item>
        <a-form-item field="scope" label="适用范围" required>
          <a-select v-model="form.scope" placeholder="选择适用范围">
            <a-option value="全部设备">全部设备</a-option>
            <a-option value="变压器">变压器</a-option>
            <a-option value="断路器">断路器</a-option>
            <a-option value="互感器">互感器</a-option>
          </a-select>
        </a-form-item>
        <a-form-item field="notify" label="通知方式">
          <a-select v-model="form.notify" placeholder="选择通知方式" multiple>
            <a-option value="站内信">站内信</a-option>
            <a-option value="邮件">邮件</a-option>
            <a-option value="短信">短信</a-option>
          </a-select>
        </a-form-item>
        <a-form-item field="enabled" label="启用">
          <a-switch v-model="form.enabled" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { Message } from '@arco-design/web-vue';
import { IconPlus, IconSearch, IconRefresh } from '@arco-design/web-vue/es/icon';
import { listRules, searchRules, createRule, updateRule, deleteRule, batchEnableRules, fromChannelsString, toChannelsString } from '../../../api/alertRules';
import type { AlertRuleDTO, ApiResp } from '../../../api/alertRules';

type Rule = {
  id: number;
  name: string;
  level: '低'|'中'|'高'|'严重';
  condition: string;
  scope: string;
  notify: string[];
  enabled: boolean;
};

// 后端/前端级别枚举映射
const toUiLevel = (s?: string): Rule['level'] => {
  const map: Record<string, Rule['level']> = {
    LOW: '低', MEDIUM: '中', HIGH: '高', URGENT: '严重',
    低: '低', 中: '中', 高: '高', 严重: '严重', 紧急: '严重'
  };
  return map[(s || '低')] || '低';
};
const toBackendLevel = (lvl: Rule['level']): string => {
  const map: Record<Rule['level'], string> = { '低': 'LOW', '中': 'MEDIUM', '高': 'HIGH', '严重': 'URGENT' };
  return map[lvl] || 'LOW';
};

const keyword = ref('');
const levelFilter = ref<string | undefined>();
const enabledFilter = ref<string | undefined>();

const rules = ref<Rule[]>([]);

const columns = [
  { title: '规则名称', dataIndex: 'name', width: 200 },
  { title: '级别', dataIndex: 'level', slotName: 'level', width: 100 },
  { title: '触发条件', dataIndex: 'condition' },
  { title: '适用范围', dataIndex: 'scope', width: 140 },
  { title: '启用', dataIndex: 'enabled', slotName: 'enabled', width: 100 },
  { title: '操作', slotName: 'actions', width: 160 }
];

const filteredRows = computed(() => {
  return rules.value.filter(r => {
    const matchKeyword = keyword.value ? (r.name.includes(keyword.value) || r.condition.includes(keyword.value)) : true;
    const matchLevel = levelFilter.value ? r.level === levelFilter.value : true;
    const matchEnabled = enabledFilter.value ? (enabledFilter.value === '启用' ? r.enabled : !r.enabled) : true;
    return matchKeyword && matchLevel && matchEnabled;
  });
});

const levelColor = (lvl: Rule['level']) => {
  const map: Record<Rule['level'], string> = { '低': 'arcoblue', '中': 'orange', '高': 'red', '严重': 'purple' };
  return map[lvl] || 'arcoblue';
};

const editVisible = ref(false);
const editMode = ref<'create'|'edit'>('create');
const form = ref<Rule>({ id: 0, name: '', level: '低', condition: '', scope: '全部设备', notify: [], enabled: true });

const openCreate = () => { editMode.value = 'create'; form.value = { id: 0, name: '', level: '低', condition: '', scope: '全部设备', notify: [], enabled: true }; editVisible.value = true; };
const editRule = (r: Rule) => { editMode.value = 'edit'; form.value = { ...r }; editVisible.value = true; };
const closeEdit = () => { editVisible.value = false; };

const loadList = async () => {
  try {
    const resp = await listRules();
    const data = ((resp as unknown as ApiResp<AlertRuleDTO[]>).data) || [];
    rules.value = data.map(d => ({
      id: Number(d.id!),
      name: d.name,
      level: toUiLevel(d.level),
      condition: d.condition || '',
      scope: d.scope || '全部设备',
      notify: fromChannelsString(d.notifyChannels),
      enabled: Boolean(d.enabled),
    }));
  } catch (e: any) {
    Message.error(e.message || '加载规则失败');
  }
};

onMounted(loadList);

const saveRule = async () => {
  if (!form.value.name || !form.value.condition) { Message.error('请填写完整的规则名称和条件'); return; }
  const payload: AlertRuleDTO = {
    name: form.value.name,
    level: toBackendLevel(form.value.level),
    condition: form.value.condition,
    scope: form.value.scope,
    notifyChannels: toChannelsString(form.value.notify),
    enabled: form.value.enabled,
  };
  try {
    if (editMode.value === 'create') {
      const resp = await createRule(payload);
      const d = (resp as unknown as ApiResp<AlertRuleDTO>).data;
      rules.value.push({
        id: Number(d.id!),
        name: d.name,
        level: toUiLevel(d.level),
        condition: d.condition || '',
        scope: d.scope || '全部设备',
        notify: fromChannelsString(d.notifyChannels),
        enabled: Boolean(d.enabled),
      });
      Message.success('规则已新增');
    } else {
      const resp = await updateRule(form.value.id, { ...payload });
      const d = (resp as unknown as ApiResp<AlertRuleDTO>).data;
      rules.value = rules.value.map(r => r.id === form.value.id ? {
        id: Number(d.id!),
        name: d.name,
        level: toUiLevel(d.level),
        condition: d.condition || '',
        scope: d.scope || '全部设备',
        notify: fromChannelsString(d.notifyChannels),
        enabled: Boolean(d.enabled),
      } : r);
      Message.success('规则已保存');
    }
    editVisible.value = false;
  } catch (e: any) {
    Message.error(e.message || '保存失败');
  }
};

const deleteRuleAction = async (r: Rule) => {
  try {
    await deleteRule(r.id);
    rules.value = rules.value.filter(x => x.id !== r.id);
    Message.success('规则已删除');
  } catch (e: any) {
    Message.error(e.message || '删除失败');
  }
};

const toggleEnable = async (r: Rule) => {
  try {
    await batchEnableRules([r.id], r.enabled);
    Message.success(r.enabled ? '规则已启用' : '规则已停用');
  } catch (e: any) {
    Message.error(e.message || '状态更新失败');
    r.enabled = !r.enabled; // 回滚
  }
};

const applyFilters = async () => {
  try {
    const enabled = enabledFilter.value ? (enabledFilter.value === '启用' ? true : false) : undefined;
    const levelParam = levelFilter.value ? toBackendLevel(levelFilter.value as Rule['level']) : undefined;
    const resp = await searchRules({ keyword: keyword.value || undefined, level: levelParam, enabled });
    const data = ((resp as unknown as ApiResp<AlertRuleDTO[]>).data) || [];
    rules.value = data.map(d => ({
      id: Number(d.id!),
      name: d.name,
      level: toUiLevel(d.level),
      condition: d.condition || '',
      scope: d.scope || '全部设备',
      notify: fromChannelsString(d.notifyChannels),
      enabled: Boolean(d.enabled),
    }));
    Message.success('筛选条件已应用');
  } catch (e: any) {
    Message.error(e.message || '筛选失败');
  }
};

const resetFilters = () => { keyword.value = ''; levelFilter.value = undefined; enabledFilter.value = undefined; };
const refresh = () => { loadList().then(() => Message.success('规则列表已刷新')); };
</script>

<style scoped>
.page-container { padding: 16px; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.page-title { font-size: 18px; font-weight: 600; margin: 8px 0; }
.filters-card { margin-bottom: 12px; }
</style>