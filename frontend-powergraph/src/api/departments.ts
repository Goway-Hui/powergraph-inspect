import request from './request';

export type ApiResp<T> = { code: number; message: string; data: T };

export type DeptDTO = {
  id: number;
  name: string;
  parentId?: number;
  leader?: string;
  status: '启用' | '禁用';
  description?: string;
};

export type DeptListParams = {
  keyword?: string;
  status?: string;
  parentId?: number;
};

export type TreeNode = { key: number; title: string; children?: TreeNode[] };

export function getDeptTree() {
  return request.get<ApiResp<TreeNode[]>>('/admin/departments/tree');
}

export function listDepts(params: DeptListParams) {
  return request.get<ApiResp<DeptDTO[]>>('/admin/departments', { params });
}

export function createDept(payload: Partial<DeptDTO>) {
  return request.post<ApiResp<DeptDTO>>('/admin/departments', payload);
}

export function updateDeptApi(id: number, payload: Partial<DeptDTO>) {
  return request.put<ApiResp<DeptDTO>>(`/admin/departments/${id}`, payload);
}

export function deleteDeptApi(id: number) {
  return request.delete<ApiResp<void>>(`/admin/departments/${id}`);
}

export function batchUpdateDeptStatus(ids: number[], status: '启用' | '禁用') {
  return request.post<ApiResp<void>>('/admin/departments/batch/status', { ids, status });
}

export function batchDeleteDepts(ids: number[]) {
  return request.post<ApiResp<void>>('/admin/departments/batch/delete', { ids });
}