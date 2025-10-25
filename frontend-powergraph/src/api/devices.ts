import request from './request';

// 统一响应结构 { code, message, data }
export type ApiResp<T> = { code: number; message: string; data: T };

export type DeviceDTO = {
  id?: number;
  code?: string;
  name: string;
  type: string;
  model?: string;
  manufacturer?: string;
  status?: string;
  location?: string;
  stationId?: number;
  departmentId?: number;
  installDate?: string;            // YYYY-MM-DD
  lastInspectionAt?: string;       // ISO 字符串
  maintenanceCycleDays?: number;
  tags?: string;                   // JSON 字符串
  createdAt?: string;
  updatedAt?: string;
};

export type ListParams = {
  keyword?: string;
  type?: string;
  status?: string;
  stationId?: number;
  departmentId?: number;
  start?: string; // ISO字符串（createdAt 起始）
  end?: string;   // ISO字符串（createdAt 结束）
};

export async function listDevices(params: ListParams = {}): Promise<ApiResp<DeviceDTO[]>> {
  return request.get('/admin/devices', { params });
}

export async function createDevice(payload: DeviceDTO): Promise<ApiResp<DeviceDTO>> {
  return request.post('/admin/devices', payload);
}

export async function updateDevice(id: number, payload: DeviceDTO): Promise<ApiResp<DeviceDTO>> {
  return request.put(`/admin/devices/${id}`, payload);
}

export async function deleteDeviceApi(id: number): Promise<ApiResp<void>> {
  return request.delete(`/admin/devices/${id}`);
}

export async function batchUpdateStatus(ids: number[], status: string): Promise<ApiResp<void>> {
  return request.post('/admin/devices/batch/status', { ids, status });
}

export async function batchDelete(ids: number[]): Promise<ApiResp<void>> {
  return request.post('/admin/devices/batch/delete', { ids });
}