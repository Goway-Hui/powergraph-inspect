<template>
  <div class="page-container">
    <div class="page-header">
      <a-breadcrumb>
        <a-breadcrumb-item>任务管理</a-breadcrumb-item>
        <a-breadcrumb-item>任务分配</a-breadcrumb-item>
      </a-breadcrumb>
      <h1 class="page-title">任务分配</h1>
      <div class="header-actions">
        <a-button type="primary" @click="submit">
          提交分配
        </a-button>
        <a-button @click="reset">
          重置
        </a-button>
      </div>
    </div>

    <a-card :bordered="false">
      <a-form :model="form" layout="vertical">
        <a-form-item label="任务名称" field="title" :rules="[{ required: true, message: '请输入任务名称' }]">
          <a-input v-model="form.title" placeholder="请输入任务名称" />
        </a-form-item>
        <a-form-item label="关联设备" field="device" :rules="[{ required: true, message: '请选择关联设备' }]">
          <a-select v-model="form.device" placeholder="请选择设备">
            <a-option v-for="d in devices" :key="d" :value="d">{{ d }}</a-option>
          </a-select>
        </a-form-item>
        <a-form-item label="巡检员" field="assignee" :rules="[{ required: true, message: '请选择巡检员' }]">
          <a-select v-model="form.assignee" placeholder="请选择巡检员">
            <a-option v-for="u in users" :key="u" :value="u">{{ u }}</a-option>
          </a-select>
        </a-form-item>
        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="优先级" field="priority" :rules="[{ required: true, message: '请选择优先级' }]">
              <a-select v-model="form.priority" placeholder="请选择">
                <a-option value="低">低</a-option>
                <a-option value="中">中</a-option>
                <a-option value="高">高</a-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="截止日期" field="dueDate" :rules="[{ required: true, message: '请选择截止日期' }]">
              <a-date-picker v-model="form.dueDate" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="任务说明" field="description">
          <a-textarea v-model="form.description" placeholder="补充任务说明（可选）" :auto-size="{ minRows: 3, maxRows: 6 }" />
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { Message } from '@arco-design/web-vue';

type AssignForm = {
  title: string;
  device: string;
  assignee: string;
  priority: '低'|'中'|'高';
  dueDate: string;
  description?: string;
};

const devices = ref<string[]>(['主变压器 A','主变压器 B','高压断路器 C','环网柜 D','温度传感器 E']);
const users = ref<string[]>(['张三','李四','王五','赵六']);

const form = ref<AssignForm>({ title: '', device: '', assignee: '', priority: '中', dueDate: '', description: '' });

const submit = () => {
  const f = form.value;
  if (!f.title || !f.device || !f.assignee || !f.dueDate) {
    Message.error('请完善分配信息');
    return;
  }
  Message.success('任务已创建并分配');
};

const reset = () => {
  form.value = { title: '', device: '', assignee: '', priority: '中', dueDate: '', description: '' };
};
</script>

<style scoped>
.page-container { padding: 16px; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.page-title { font-size: 18px; font-weight: 600; margin: 8px 0; }
.header-actions { display: flex; gap: 8px; }
</style>