import request from './request';

export type ApiResp<T> = {
  code: number;
  message: string;
  data: T;
};

export type AlertDTO = {
  id?: number;
  deviceId?: number;
  level?: string;
  content?: string;
  status?: string;
  assignedTo?: number;
  resolvedAt?: string;
  createdAt?: string;
  updatedAt?: string;
};

export type ListParams = {
  keyword?: string;
  status?: string;
  level?: string;
  deviceId?: number;
  assignedTo?: number;
};

export async function listAlerts() {
  return request.get<ApiResp<AlertDTO[]>>('/admin/alerts/list');
}

export async function getAlert(id: number) {
  return request.get<ApiResp<AlertDTO>>(`/admin/alerts/${id}`);
}

export async function createAlert(payload: AlertDTO) {
  return request.post<ApiResp<AlertDTO>>('/admin/alerts/create', payload);
}

export async function updateAlert(id: number, payload: AlertDTO) {
  return request.put<ApiResp<AlertDTO>>(`/admin/alerts/${id}`, payload);
}

export async function deleteAlert(id: number) {
  return request.delete<ApiResp<void>>(`/admin/alerts/${id}`);
}

export async function searchAlerts(params: ListParams) {
  return request.post<ApiResp<AlertDTO[]>>('/admin/alerts/search', params);
}

export async function batchUpdateAlertStatus(ids: number[], status: string) {
  return request.post<ApiResp<void>>('/admin/alerts/batch/status', { ids, status });
}

export async function batchDeleteAlerts(ids: number[]) {
  return request.post<ApiResp<void>>('/admin/alerts/batch/delete', { ids });
}

export async function assignAlert(id: number, userId: number) {
  return request.post<ApiResp<AlertDTO>>(`/admin/alerts/${id}/assign`, { userId });
}

export async function resolveAlert(id: number) {
  return request.post<ApiResp<AlertDTO>>(`/admin/alerts/${id}/resolve`, {});
}