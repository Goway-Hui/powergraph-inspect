import request from './request';

export type ApiResp<T> = { code: number; message: string; data: T };

export type RoleDTO = {
  id: number;
  name: string;
  code: string;
  description?: string;
  status: '启用' | '禁用';
  permissions?: string; // 后端以JSON字符串返回
  createdAt?: string;
  updatedAt?: string;
};

export type ListParams = {
  keyword?: string;
  status?: string;
  start?: string; // ISO string
  end?: string;   // ISO string
};

export function listRoles(params: ListParams) {
  return request.get<ApiResp<RoleDTO[]>>('/admin/roles', { params });
}

export function createRole(payload: Partial<RoleDTO>) {
  return request.post<ApiResp<RoleDTO>>('/admin/roles', payload);
}

export function updateRoleApi(id: number, payload: Partial<RoleDTO>) {
  return request.put<ApiResp<RoleDTO>>(`/admin/roles/${id}`, payload);
}

export function deleteRoleApi(id: number) {
  return request.delete<ApiResp<void>>(`/admin/roles/${id}`);
}

export function batchUpdateRoleStatus(ids: number[], status: '启用' | '禁用') {
  return request.post<ApiResp<void>>('/admin/roles/batch/status', { ids, status });
}

export function batchDeleteRoles(ids: number[]) {
  return request.post<ApiResp<void>>('/admin/roles/batch/delete', { ids });
}