import request from './request';

// 后端统一响应结构 { code, message, data }
export type ApiResp<T> = { code: number; message: string; data: T };

export type UserDTO = {
  id?: number;
  username: string;
  password?: string;
  name: string;
  role: 'ADMIN' | 'INSPECTOR' | 'VIEWER';
  status?: '启用' | '禁用';
  avatar?: string;
  createdAt?: string;
  updatedAt?: string;
};

export type ListParams = {
  keyword?: string;
  role?: 'ADMIN' | 'INSPECTOR' | 'VIEWER';
  status?: '启用' | '禁用';
  start?: string; // ISO字符串
  end?: string;   // ISO字符串
};

export async function listUsers(params: ListParams = {}): Promise<ApiResp<UserDTO[]>> {
  return request.get('/admin/users', { params });
}

export async function createUser(payload: UserDTO): Promise<ApiResp<UserDTO>> {
  return request.post('/admin/users', payload);
}

export async function updateUser(id: number, payload: UserDTO): Promise<ApiResp<UserDTO>> {
  return request.put(`/admin/users/${id}`, payload);
}

export async function deleteUserApi(id: number): Promise<ApiResp<void>> {
  return request.delete(`/admin/users/${id}`);
}

export async function batchUpdateStatus(ids: number[], status: '启用' | '禁用'): Promise<ApiResp<void>> {
  return request.post('/admin/users/batch/status', { ids, status });
}

export async function batchDelete(ids: number[]): Promise<ApiResp<void>> {
  return request.post('/admin/users/batch/delete', { ids });
}