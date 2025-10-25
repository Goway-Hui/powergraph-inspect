import request from './request';

// 统一响应结构
export type ApiResp<T> = { code: number; message: string; data: T };

export type TaskDTO = {
  id?: number;
  title: string;
  description?: string;
  deviceId?: number;
  assigneeId?: number;
  departmentId?: number;
  creatorId?: number;
  stationId?: number;
  tags?: string;
  status?: 'PENDING' | 'IN_PROGRESS' | 'COMPLETED';
  priority?: 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
  deadline?: string;       // ISO 字符串
  completedAt?: string;    // ISO 字符串
  createdAt?: string;
  updatedAt?: string;
};

export type ListParams = {
  keyword?: string;
  status?: string;
  priority?: string;
  assigneeId?: number;
  deviceId?: number;
  departmentId?: number;
  stationId?: number;
};

export async function listTasks(params: ListParams = {}): Promise<ApiResp<TaskDTO[]>> {
  return request.get('/admin/tasks', { params });
}

export async function createTask(payload: TaskDTO): Promise<ApiResp<TaskDTO>> {
  return request.post('/admin/tasks', payload);
}

export async function updateTask(id: number, payload: TaskDTO): Promise<ApiResp<TaskDTO>> {
  return request.put(`/admin/tasks/${id}`, payload);
}

export async function deleteTaskApi(id: number): Promise<ApiResp<void>> {
  return request.delete(`/admin/tasks/${id}`);
}

export async function batchUpdateTaskStatus(ids: number[], status: string): Promise<ApiResp<void>> {
  return request.post('/admin/tasks/batch/status', { ids, status });
}

export async function batchDeleteTasks(ids: number[]): Promise<ApiResp<void>> {
  return request.post('/admin/tasks/batch/delete', { ids });
}

export async function assignTask(id: number, assigneeId: number): Promise<ApiResp<TaskDTO>> {
  return request.post('/admin/tasks/assign', { id, assigneeId });
}

export async function completeTask(id: number): Promise<ApiResp<TaskDTO>> {
  return request.post(`/admin/tasks/${id}/complete`);
}