import request from './request';

export interface ApiResp<T> { code: number; message: string; data: T; }

export interface AlertRuleDTO {
  id?: number;
  name: string;
  level?: string;
  condition?: string;
  scope?: string;
  notifyChannels?: string; // 逗号分隔字符串
  enabled?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

// 辅助转换：数组 <-> 逗号分隔字符串
export function toChannelsString(arr?: string[]): string | undefined {
  if (!arr) return undefined;
  return arr.join(',');
}

export function fromChannelsString(s?: string): string[] {
  if (!s) return [];
  return s.split(',').map(v => v.trim()).filter(Boolean);
}

const base = '/admin/alert-rules';

export function listRules() {
  return request.get<ApiResp<AlertRuleDTO[]>>(`${base}/list`);
}

export function getRule(id: number) {
  return request.get<ApiResp<AlertRuleDTO>>(`${base}/${id}`);
}

export function createRule(rule: AlertRuleDTO) {
  return request.post<ApiResp<AlertRuleDTO>>(`${base}/create`, rule);
}

export function updateRule(id: number, rule: AlertRuleDTO) {
  return request.put<ApiResp<AlertRuleDTO>>(`${base}/${id}`, rule);
}

export function deleteRule(id: number) {
  return request.delete<ApiResp<void>>(`${base}/${id}`);
}

export function searchRules(params: { keyword?: string; level?: string; enabled?: boolean; }) {
  return request.post<ApiResp<AlertRuleDTO[]>>(`${base}/search`, params);
}

export function batchEnableRules(ids: number[], enabled: boolean) {
  return request.post<ApiResp<void>>(`${base}/batch/enable`, { ids, enabled });
}

export function batchDeleteRules(ids: number[]) {
  return request.post<ApiResp<void>>(`${base}/batch/delete`, { ids });
}