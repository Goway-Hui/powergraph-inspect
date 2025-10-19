import request from './request';

// 获取图谱数据
export const getGraphData = () => {
  return request({
    url: '/graph/data',
    method: 'get'
  });
};

// 添加节点
export const addNode = (nodeData: any) => {
  return request({
    url: '/graph/node',
    method: 'post',
    data: nodeData
  });
};

// 更新节点
export const updateNode = (nodeId: string, nodeData: any) => {
  return request({
    url: `/graph/node/${nodeId}`,
    method: 'put',
    data: nodeData
  });
};

// 删除节点
export const deleteNode = (nodeId: string) => {
  return request({
    url: `/graph/node/${nodeId}`,
    method: 'delete'
  });
};

// 添加关系
export const addEdge = (edgeData: any) => {
  return request({
    url: '/graph/edge',
    method: 'post',
    data: edgeData
  });
};

// 更新关系
export const updateEdge = (edgeId: string, edgeData: any) => {
  return request({
    url: `/graph/edge/${edgeId}`,
    method: 'put',
    data: edgeData
  });
};

// 删除关系
export const deleteEdge = (edgeId: string) => {
  return request({
    url: `/graph/edge/${edgeId}`,
    method: 'delete'
  });
};