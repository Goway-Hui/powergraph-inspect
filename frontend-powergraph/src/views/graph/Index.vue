<template>
  <a-layout-content class="content">
    <div class="graph-header">
      <div class="header-info">
        <a-breadcrumb>
          <a-breadcrumb-item>首页</a-breadcrumb-item>
          <a-breadcrumb-item>知识图谱</a-breadcrumb-item>
          <a-breadcrumb-item>图谱可视化</a-breadcrumb-item>
        </a-breadcrumb>
        <h1 class="page-title">知识图谱可视化</h1>
      </div>
      <div class="graph-actions">
        <a-button @click="handleSave" type="primary">
          <icon-save />
          保存视图
        </a-button>
        <a-button @click="handleAddNode" type="primary">
          <icon-plus />
          添加节点
        </a-button>
        <a-button @click="handleAddEdge" type="primary">
          <icon-plus />
          添加关系
        </a-button>
        <a-input-search
          style="width: 240px; margin-left: 12px;"
          allow-clear
          placeholder="搜索节点ID或名称"
          @search="handleSearch"
        />
      </div>
    </div>
    
    <a-row :gutter="16" class="graph-main">
      <a-col :span="18">
        <!-- 图谱可视化区域 -->
        <a-card class="graph-container" :bordered="false">
          <template #title>
            <div class="graph-title">
              <icon-mind-mapping />
              <span>图谱展示区</span>
            </div>
          </template>
          <template #extra>
            <div class="graph-controls">
              <a-space>
                <a-button @click="handleZoomIn" size="small">
                  <icon-zoom-in />
                </a-button>
                <a-button @click="handleZoomOut" size="small">
                  <icon-zoom-out />
                </a-button>
                <a-button @click="handleReset" size="small">
                  <icon-undo />
                </a-button>
                <a-button @click="handleAutoLayout" size="small">
                  <icon-send />
                </a-button>
                <a-button @click="handleFullScreen" size="small">
                  <icon-fullscreen />
                </a-button>
                <a-button @click="handleRefresh" size="small">
                  <icon-refresh />
                </a-button>
              </a-space>
            </div>
          </template>
          <div ref="graphCanvas" class="graph-canvas"></div>
        </a-card>
      </a-col>

      <a-col :span="6">
        <!-- 图谱控制面板 -->
        <a-card title="图谱控制" class="graph-panel" :bordered="false">
          <a-form :model="graphConfig" layout="vertical">
            <a-form-item label="布局算法">
              <a-select v-model="graphConfig.layout">
                <a-option value="force">力导向布局</a-option>
                <a-option value="circle">圆形布局</a-option>
                <a-option value="grid">网格布局</a-option>
                <a-option value="tree">树形布局</a-option>
              </a-select>
            </a-form-item>

            <a-form-item label="节点类型筛选">
              <a-checkbox-group v-model="graphConfig.nodeTypes" direction="vertical">
                <a-checkbox value="station">变电站</a-checkbox>
                <a-checkbox value="device">设备</a-checkbox>
                <a-checkbox value="task">任务</a-checkbox>
                <a-checkbox value="alert">告警</a-checkbox>
                <a-checkbox value="person">人员</a-checkbox>
                <a-checkbox value="document">文档</a-checkbox>
              </a-checkbox-group>
            </a-form-item>

            <a-form-item label="关系类型筛选">
              <a-checkbox-group v-model="graphConfig.edgeTypes" direction="vertical">
                <a-checkbox value="LOCATED_IN">归属关系</a-checkbox>
                <a-checkbox value="INSPECTS">巡检关系</a-checkbox>
                <a-checkbox value="ASSIGNED_TO">分配关系</a-checkbox>
                <a-checkbox value="AFFECTS">影响关系</a-checkbox>
                <a-checkbox value="RELATED_TO">关联关系</a-checkbox>
                <a-checkbox value="DOCUMENTS">文档关系</a-checkbox>
              </a-checkbox-group>
            </a-form-item>

            <a-form-item>
              <a-button long @click="applyFilters" type="primary">
                <icon-filter />
                应用筛选
              </a-button>
            </a-form-item>
          </a-form>
        </a-card>

        <!-- 图谱信息面板 -->
        <a-card title="图谱信息" class="graph-info" :bordered="false">
          <a-descriptions :column="1" size="small">
            <a-descriptions-item label="节点总数">
              <a-tag color="arcoblue">{{ graphStats.nodes }}</a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="关系总数">
              <a-tag color="green">{{ graphStats.edges }}</a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="图谱深度">
              <a-tag color="purple">{{ graphStats.depth }}</a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="最后更新">
              <span class="update-time">{{ graphStats.lastUpdate }}</span>
            </a-descriptions-item>
            <a-descriptions-item label="数据源">
              <a-tag color="orange">Neo4j图数据库</a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="状态">
              <a-tag :color="graphStats.status === '正常' ? 'green' : 'red'">{{ graphStats.status }}</a-tag>
            </a-descriptions-item>
          </a-descriptions>
        </a-card>
      </a-col>
    </a-row>

    <!-- 节点详情抽屉 -->
    <a-drawer
      :visible="drawerVisible"
      @cancel="drawerVisible = false"
      @before-ok="handleDrawerOk"
      :width="400"
      title="节点详情"
    >
      <a-descriptions
        v-if="selectedNode"
        :data="nodeDetails"
        :column="1"
        bordered
        size="small"
      />
      <div class="drawer-actions" v-if="selectedNode">
        <a-button long style="margin-top: 16px;" @click="handleEditNode">
          <icon-edit />
          编辑节点
        </a-button>
        <a-button long style="margin-top: 8px;" @click="handleDeleteNode">
          <icon-delete />
          删除节点
        </a-button>
      </div>
    </a-drawer>

    <!-- 关系详情抽屉 -->
    <a-drawer
      :visible="edgeDrawerVisible"
      @cancel="edgeDrawerVisible = false"
      :width="420"
      title="关系详情"
    >
      <a-descriptions
        v-if="selectedEdge"
        :data="edgeDetails"
        :column="1"
        bordered
        size="small"
      />
      <div class="drawer-actions" v-if="selectedEdge">
        <a-button long style="margin-top: 16px;" @click="handleEditEdge">
          <icon-edit />
          编辑关系
        </a-button>
        <a-button long style="margin-top: 8px;" @click="handleDeleteEdge">
          <icon-delete />
          删除关系
        </a-button>
      </div>
    </a-drawer>

    <!-- 添加/编辑节点模态框 -->
    <a-modal
      v-model:visible="nodeModalVisible"
      :title="editingNode ? '编辑节点' : '添加节点'"
      @ok="handleSaveNode"
      @cancel="nodeModalVisible = false"
    >
      <a-form :model="nodeForm" layout="vertical">
        <a-form-item label="节点ID">
          <a-input v-model="nodeForm.id" :disabled="!!editingNode" />
        </a-form-item>
        <a-form-item label="节点名称">
          <a-input v-model="nodeForm.name" />
        </a-form-item>
        <a-form-item label="节点类型">
          <a-select v-model="nodeForm.type">
            <a-option value="station">变电站</a-option>
            <a-option value="device">设备</a-option>
            <a-option value="task">任务</a-option>
            <a-option value="alert">告警</a-option>
            <a-option value="person">人员</a-option>
            <a-option value="document">文档</a-option>
          </a-select>
        </a-form-item>
        <a-form-item label="其他属性">
          <a-input v-model="nodeForm.properties" placeholder='JSON格式，如: {"key": "value"}' />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 添加/编辑关系模态框 -->
    <a-modal
      v-model:visible="edgeModalVisible"
      :title="editingEdge ? '编辑关系' : '添加关系'"
      @ok="handleSaveEdge"
      @cancel="edgeModalVisible = false"
    >
      <a-form :model="edgeForm" layout="vertical">
        <a-form-item label="关系类型">
          <a-input v-model="edgeForm.relationship" placeholder="如: LOCATED_IN / INSPECTS" />
        </a-form-item>
        <a-form-item label="起点节点ID">
          <a-input v-model="edgeForm.source" :disabled="!!editingEdge" />
        </a-form-item>
        <a-form-item label="终点节点ID">
          <a-input v-model="edgeForm.target" :disabled="!!editingEdge" />
        </a-form-item>
        <a-form-item label="其他属性">
          <a-input v-model="edgeForm.properties" placeholder='JSON格式，如: {"weight": 1}' />
        </a-form-item>
      </a-form>
    </a-modal>
  </a-layout-content>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick, watch } from 'vue';
import { Message } from '@arco-design/web-vue';

import cytoscape from 'cytoscape';
import cola from 'cytoscape-cola';
// @ts-ignore

import {
  IconZoomIn,
  IconZoomOut,
  IconUndo,
  IconSend,
  IconFullscreen,
  IconSave,
  IconFilter,
  IconEdit,
  IconDelete,
  IconPlus,
  IconRefresh
} from '@arco-design/web-vue/es/icon';
import { IconMindMapping } from '@arco-design/web-vue/es/icon';
import { getGraphData, addNode, updateNode, deleteNode, addEdge, updateEdge, deleteEdge } from '../../api/graph';
import { getNodeStyle, getEdgeStyle } from '../../utils/graphUtils';

cytoscape.use(cola);

// ... 其他代码保持不变 ...
const graphCanvas = ref<HTMLElement | null>(null);
// ... 其他 ref 和 reactive 定义 ...
const drawerVisible = ref(false);
const editingNode = ref<any>(null);
const nodeModalVisible = ref(false);
const selectedNode = ref<any>(null); // 用于存储当前点击的节点对象

// 关系相关状态
const edgeDrawerVisible = ref(false);
const selectedEdge = ref<any>(null);
const editingEdge = ref<any>(null);
const edgeModalVisible = ref(false);
const edgeForm = reactive({ relationship: '', source: '', target: '', properties: '' });


let cy: any = null;
const graphConfig = reactive({
  layout: 'cola',
  nodeTypes: ['station', 'device', 'task', 'alert', 'person', 'document'],
  edgeTypes: ['LOCATED_IN', 'INSPECTS', 'ASSIGNED_TO', 'AFFECTS', 'RELATED_TO', 'DOCUMENTS']
});
const nodeForm = reactive({ id: '', name: '', type: 'device', properties: '' });
const graphStats = reactive({ nodes: 0, edges: 0, depth: 0, lastUpdate: '2025-10-14 10:00', status: '正常' });
const nodeDetails = ref<any[]>([]);
const edgeDetails = ref<any[]>([]);

// 原始数据缓存，供过滤与重新渲染使用
const rawNodes = ref<any[]>([]);
const rawEdges = ref<any[]>([]);

// 将当前选择映射到 Cytoscape 布局名称
const resolveLayoutName = (name: string) => {
  // 前端展示的“力导向布局”对应 cytoscape 的 cola
  if (name === 'force' || name === 'cola') return 'cola';
  if (name === 'tree') return 'breadthfirst';
  if (name === 'circle') return 'circle';
  if (name === 'grid') return 'grid';
  return 'cola';
};

// 根据勾选的节点/关系类型进行过滤
const filterData = (nodes: any[], edges: any[]) => {
  const nodeTypesSet = new Set(graphConfig.nodeTypes);
  const edgeTypesSet = new Set(graphConfig.edgeTypes);

  const filteredNodes = nodes.filter((n: any) => {
    // 若无类型信息则不过滤
    return !n.type || nodeTypesSet.has(String(n.type));
  });

  const filteredEdges = edges.filter((e: any) => {
    const rel = e.relationship || e.type;
    return !rel || edgeTypesSet.has(String(rel));
  });

  return { filteredNodes, filteredEdges };
};


// --- 【核心修改：在这里添加诊断日志】 ---

// 加载图谱数据
const loadGraphData = async () => {
  console.log("1. [loadGraphData] 开始加载图谱数据...");
  try {
    const response = await getGraphData();
    console.log("2. [loadGraphData] 已从后端接收到响应:", response);

    const data = response.data || response;
    const nodes = data.nodes || [];
    const edges = data.edges || [];

    // 缓存原始数据，供过滤与重新渲染
    rawNodes.value = nodes;
    rawEdges.value = edges;

    console.log(`3. [loadGraphData] 解析出的节点数量: ${nodes.length}, 边数量: ${edges.length}`);
    console.log("   [节点数据样本]:", nodes.length > 0 ? nodes[0] : "无");
    console.log("   [边数据样本]:", edges.length > 0 ? edges[0] : "无");

    graphStats.nodes = nodes.length;
    graphStats.edges = edges.length;
    graphStats.lastUpdate = new Date().toLocaleString();

    if (!cy) {
      console.log("4. [loadGraphData] 图谱未初始化，准备调用 initGraph...");
      initGraph(nodes, edges);
    } else {
      console.log("4. [loadGraphData] 图谱已存在，准备调用 updateGraph...");
      updateGraph(nodes, edges);
    }
  } catch (error: any) {
    Message.error('数据加载失败: ' + (error.message || '未知错误'));
    console.error('Failed to load graph data:', error);
  }
};

// 初始化图谱
const initGraph = (nodes: any[], edges: any[]) => {
  console.log("5. [initGraph] initGraph 函数被调用。");

  if (graphCanvas.value) {
    console.log("6. [initGraph] 成功找到 graphCanvas DOM 元素。");

    const cytoscapeNodes = nodes.map(node => ({
      data: {
        id: node.id,
        name: node.name || node.title || node.description || node.label || node.id,
        ...node
      }
    }));

    const cytoscapeEdges = edges.map((edge, idx) => ({
      data: {
        // 修复：边ID必须唯一，包含source/target/关系/索引，避免多关系覆盖
        id: edge.id || `${edge.source}-${edge.target}-${edge.relationship || edge.type || 'rel'}-${idx}`,
        source: edge.source,
        target: edge.target,
        label: edge.relationship || edge.type || '',
        ...edge
      }
    }));

    console.log("7. [initGraph] 准备创建 Cytoscape 实例...");
    cy = cytoscape({
      container: graphCanvas.value,
      elements: {
        nodes: cytoscapeNodes,
        edges: cytoscapeEdges
      },
      // ... 样式和布局代码 ...
      style: [
        {
          selector: 'node',
          style: {
            'background-color': '#00B42A',
            'label': 'data(name)',
            'color': '#ffffff',
            'text-valign': 'center',
            'text-halign': 'center',
            'font-size': '12px',
            'width': 50,
            'height': 50,
            'events': 'yes'
          }
        },
        { selector: 'node[type = "station"]', style: { 'background-color': '#165DFF', 'shape': 'rectangle', 'width': 60, 'height': 40 } },
        { selector: 'node[type = "device"]', style: { 'background-color': '#00B42A', 'shape': 'ellipse', 'width': 50, 'height': 50 } },
        { selector: 'node[type = "task"]', style: { 'background-color': '#FF7D00', 'shape': 'round-rectangle', 'width': 50, 'height': 30 } },
        { selector: 'node[type = "alert"]', style: { 'background-color': '#F53F3F', 'shape': 'diamond', 'width': 40, 'height': 40 } },
        { selector: 'node[type = "person"]', style: { 'background-color': '#7B68EE', 'shape': 'ellipse', 'width': 45, 'height': 45 } },
        { selector: 'node[type = "document"]', style: { 'background-color': '#FFB400', 'shape': 'round-rectangle', 'width': 50, 'height': 30 } },
        {
          selector: 'edge',
          style: {
            'width': 2,
            'line-color': '#888',
            'target-arrow-color': '#888',
            'target-arrow-shape': 'triangle',
            'curve-style': 'bezier',
            'label': 'data(label)',
            'font-size': '10px',
            'text-background-color': '#ffffff',
            'text-background-opacity': 1,
            'events': 'yes'
          }
        },
        { selector: 'edge[relationship = "LOCATED_IN"]', style: { 'line-color': '#165DFF', 'target-arrow-color': '#165DFF' } },
        { selector: 'edge[relationship = "INSPECTS"]', style: { 'line-color': '#00B42A', 'target-arrow-color': '#00B42A' } },
        { selector: 'edge[relationship = "ASSIGNED_TO"]', style: { 'line-color': '#FF7D00', 'target-arrow-color': '#FF7D00' } },
        { selector: 'edge[relationship = "AFFECTS"]', style: { 'line-color': '#F53F3F', 'target-arrow-color': '#F53F3F' } },
        { selector: 'edge[relationship = "RELATED_TO"]', style: { 'line-color': '#7B68EE', 'target-arrow-color': '#7B68EE' } },
        { selector: 'edge[relationship = "DOCUMENTS"]', style: { 'line-color': '#FFB400', 'target-arrow-color': '#FFB400' } }
      ],
      layout: { name: resolveLayoutName(graphConfig.layout) }
    });
    console.log("8. [initGraph] Cytoscape 实例已创建。");
    // 初始化后自适应视图，避免只显示局部
    try {
      cy.fit();
    } catch (e) {
      console.warn('cy.fit 调用失败', e);
    }
    // 全局事件调试：确认 Cytoscape 能收到点击
    cy.on('tap', (evt: any) => {
      const tgt = evt.target;
      const group = tgt && tgt.group ? tgt.group() : 'core';
      console.log('[cy] tap event on:', group);
      // 如果点击被认为是 core（例如点在文字区域），尝试回退为“最近节点”
      if (group === 'core') {
        const pos = evt.position; // 模型坐标
        let nearest: any = null;
        let best = Infinity;
        cy.nodes().forEach((n: any) => {
          const p = n.position();
          const dx = p.x - pos.x;
          const dy = p.y - pos.y;
          const d = Math.sqrt(dx * dx + dy * dy);
          if (d < best) {
            best = d;
            nearest = n;
          }
        });
        if (nearest) {
          const threshold = Math.max(nearest.width(), nearest.height()) / 2 + 10;
          if (best <= threshold) {
            console.log('[cy] fallback to nearest node:', nearest.data());
            selectedNode.value = nearest;
            nodeDetails.value = [
              { label: '节点ID', value: nearest.data('id') },
              { label: '名称', value: nearest.data('name') },
              { label: '类型', value: nearest.data('type') || '' }
            ];
            drawerVisible.value = true;
          }
        }
      }
    });
    cy.on('click', (evt: any) => {
      const tgt = evt.target;
      const group = tgt && tgt.group ? tgt.group() : 'core';
      console.log('[cy] click event on:', group);
    });
    // 绑定点击事件：节点与关系
    cy.on('tap', 'node', (evt: any) => {
      const n = evt.target;
      console.log('[cy] node tapped:', n && n.data ? n.data() : n);
      selectedNode.value = n;
      nodeDetails.value = [
        { label: '节点ID', value: n.data('id') },
        { label: '名称', value: n.data('name') },
        { label: '类型', value: n.data('type') || '' }
      ];
      drawerVisible.value = true;
    });
    cy.on('click', 'node', (evt: any) => {
      const n = evt.target;
      selectedNode.value = n;
      nodeDetails.value = [
        { label: '节点ID', value: n.data('id') },
        { label: '名称', value: n.data('name') },
        { label: '类型', value: n.data('type') || '' }
      ];
      drawerVisible.value = true;
    });

    cy.on('tap', 'edge', (evt: any) => {
      const e = evt.target;
      console.log('[cy] edge tapped:', e && e.data ? e.data() : e);
      selectedEdge.value = e;
      edgeDetails.value = [
        { label: '关系ID', value: e.data('id') },
        { label: '类型', value: e.data('relationship') || e.data('type') || '' },
        { label: '起点', value: e.data('source') },
        { label: '终点', value: e.data('target') }
      ];
      edgeDrawerVisible.value = true;
    });
    cy.on('click', 'edge', (evt: any) => {
      const e = evt.target;
      selectedEdge.value = e;
      edgeDetails.value = [
        { label: '关系ID', value: e.data('id') },
        { label: '类型', value: e.data('relationship') || e.data('type') || '' },
        { label: '起点', value: e.data('source') },
        { label: '终点', value: e.data('target') }
      ];
      edgeDrawerVisible.value = true;
    });
  } else {
    console.error("X. [initGraph] 错误：graphCanvas DOM 元素未找到！");
  }
};

// ... 其他所有方法 (updateGraph, onMounted, 等) 保持不变 ...
const updateGraph = (nodes: any[], edges: any[]) => {
  if (!cy) return;

  // 过滤数据后再渲染
  const { filteredNodes, filteredEdges } = filterData(nodes, edges);

  cy.elements().remove();

  filteredNodes.forEach((node: any) => {
    cy.add({
      group: 'nodes',
      data: {
        id: node.id,
        name: node.name || node.title || node.description || node.label || node.id,
        ...node
      }
    });
  });

  filteredEdges.forEach((edge: any, idx: number) => {
    cy.add({
      group: 'edges',
      data: {
        // 与 initGraph 保持一致的唯一ID生成
        id: edge.id || `${edge.source}-${edge.target}-${edge.relationship || edge.type || 'rel'}-${idx}`,
        source: edge.source,
        target: edge.target,
        label: edge.relationship || edge.type || '',
        ...edge
      }
    });
  });

  const layout = cy.layout({ name: resolveLayoutName(graphConfig.layout) });
  layout.run();
  cy.fit();
};

onMounted(() => {
  nextTick(() => {
    loadGraphData();
    // 响应式：监听窗口尺寸变化，保持画布自适应
    window.addEventListener('resize', () => {
      if (cy) {
        cy.resize();
        cy.fit();
      }
    });
  });
});

// 当用户在右侧面板切换布局算法时，立即重新布局
watch(
  () => graphConfig.layout,
  (layoutName) => {
    if (cy) {
      const layout = cy.layout({ name: resolveLayoutName(layoutName) });
      layout.run();
      cy.fit();
    }
  }
);
// ... 其他所有 handleXXX 方法 ...
const handleZoomIn = () => { if (cy) { cy.zoom({ level: cy.zoom() * 1.2, renderedPosition: { x: cy.width() / 2, y: cy.height() / 2 } }); } };
const handleZoomOut = () => { if (cy) { cy.zoom({ level: cy.zoom() * 0.8, renderedPosition: { x: cy.width() / 2, y: cy.height() / 2 } }); } };
const handleReset = () => { if (cy) { cy.fit(); } };
const handleAutoLayout = () => {
  if (cy) {
    const layout = cy.layout({ name: resolveLayoutName(graphConfig.layout) });
    layout.run();
    cy.fit();
  }
};
const handleFullScreen = () => { Message.info('全屏显示'); };
const handleRefresh = () => { loadGraphData(); };
const handleSave = () => { Message.success('视图已保存'); };
const applyFilters = () => {
  updateGraph(rawNodes.value, rawEdges.value);
  Message.success('筛选条件已应用');
};
const handleDrawerOk = () => { drawerVisible.value = false; return true; };
const handleAddNode = () => { editingNode.value = null; nodeForm.id = ''; nodeForm.name = ''; nodeForm.type = 'device'; nodeForm.properties = ''; nodeModalVisible.value = true; };
const handleEditNode = () => { if (selectedNode.value) { editingNode.value = selectedNode.value; nodeForm.id = selectedNode.value.data('id'); nodeForm.name = selectedNode.value.data('name'); nodeForm.type = selectedNode.value.data('type') || 'device'; nodeForm.properties = JSON.stringify(selectedNode.value.data()); nodeModalVisible.value = true; } };
const handleDeleteNode = async () => { if (selectedNode.value) { try { await deleteNode(selectedNode.value.data('id')); Message.success('节点删除成功'); drawerVisible.value = false; loadGraphData(); } catch (error) { Message.error('节点删除失败'); } } };
const handleSaveNode = async () => {
  try {
    // 基础校验
    if (!nodeForm.type) { Message.error('请选择节点类型'); return; }
    if (!nodeForm.name || !nodeForm.name.trim()) { Message.error('请填写节点名称'); return; }

    let properties: any = {};
    try {
      properties = nodeForm.properties ? JSON.parse(nodeForm.properties) : {};
    } catch (e) {
      Message.error('属性格式不正确，请输入有效的JSON格式');
      return;
    }

    // 组装参数
    const nodeData: any = { id: nodeForm.id, name: nodeForm.name, type: nodeForm.type, ...properties };

    if (editingNode.value) {
      await updateNode(nodeForm.id, nodeData);
      Message.success('节点更新成功');
      nodeModalVisible.value = false;
      loadGraphData();
    } else {
      const resp = await addNode(nodeData);
      const created = resp?.data?.data || resp?.data || {};
      Message.success('节点添加成功');
      nodeModalVisible.value = false;
      loadGraphData();
      // 若后端返回了新节点ID，则尝试聚焦该节点
      nextTick(() => {
        try {
          const createdId = created?.id;
          if (createdId && cy) {
            const ele = cy.getElementById(String(createdId));
            if (ele) { ele.select(); cy.center(ele); }
          }
        } catch (e) { /* 忽略聚焦失败 */ }
      });
    }
  } catch (error) {
    Message.error('操作失败');
  }
};

// 添加/编辑/删除关系
const handleAddEdge = () => {
  editingEdge.value = null;
  edgeForm.relationship = '';
  edgeForm.source = selectedNode.value ? String(selectedNode.value.data('id')) : '';
  edgeForm.target = '';
  edgeForm.properties = '';
  edgeModalVisible.value = true;
};

const handleEditEdge = () => {
  if (selectedEdge.value) {
    editingEdge.value = selectedEdge.value;
    edgeForm.relationship = selectedEdge.value.data('relationship') || selectedEdge.value.data('type') || '';
    edgeForm.source = String(selectedEdge.value.data('source'));
    edgeForm.target = String(selectedEdge.value.data('target'));
    edgeForm.properties = JSON.stringify({});
    edgeModalVisible.value = true;
  }
};

const handleDeleteEdge = async () => {
  if (selectedEdge.value) {
    try {
      await deleteEdge(String(selectedEdge.value.data('id')));
      Message.success('关系删除成功');
      edgeDrawerVisible.value = false;
      loadGraphData();
    } catch (error) {
      Message.error('关系删除失败');
    }
  }
};

const handleSaveEdge = async () => {
  try {
    if (!edgeForm.relationship || !edgeForm.relationship.trim()) { Message.error('请填写关系类型'); return; }
    if (!edgeForm.source || !edgeForm.target) { Message.error('请填写起点与终点节点ID'); return; }

    let properties: any = {};
    try {
      properties = edgeForm.properties ? JSON.parse(edgeForm.properties) : {};
    } catch (e) {
      Message.error('属性格式不正确，请输入有效的JSON格式');
      return;
    }

    const edgeData: any = { relationship: edgeForm.relationship, source: edgeForm.source, target: edgeForm.target, ...properties };

    if (editingEdge.value) {
      const edgeId = String(editingEdge.value.data('id'));
      await updateEdge(edgeId, edgeData);
      Message.success('关系更新成功');
      edgeModalVisible.value = false;
      loadGraphData();
    } else {
      await addEdge(edgeData);
      Message.success('关系添加成功');
      edgeModalVisible.value = false;
      loadGraphData();
    }
  } catch (error) {
    Message.error('操作失败');
  }
};

// 搜索查询并聚焦
const handleSearch = (keyword: string) => {
  const kw = (keyword || '').trim().toLowerCase();
  if (!kw || !cy) return;
  const matches = cy.nodes().filter((n: any) => {
    const id = String(n.data('id') || '').toLowerCase();
    const name = String(n.data('name') || '').toLowerCase();
    return id.includes(kw) || name.includes(kw);
  });
  if (matches.length === 0) {
    Message.info('未找到匹配的节点');
    return;
  }
  const target = matches[0];
  target.select();
  cy.center(target);
  Message.success(`找到 ${matches.length} 个匹配节点，已定位首个`);
};

</script>

<style scoped>
.graph-container {
  height: 100%;
  width: 100%;
}

.graph-canvas {
  /* 响应式高度：随视窗变化，避免容器过小导致图谱显示不全 */
  height: calc(100vh - 220px);
  width: 100%;
  display: block;
  box-sizing: border-box;
  position: relative;
  z-index: 3; /* 确保在抽屉/面板之下但高于其他遮挡层 */
  pointer-events: auto;
}

.graph-header {
  padding: 16px 24px;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-info {
  flex: 1;
}

.graph-actions {
  display: flex;
  gap: 12px;
}

.graph-main {
  margin-top: 16px;
}

.graph-panel {
  margin-bottom: 16px;
}

.graph-info {
  margin-bottom: 16px;
}

.update-time {
  color: #666;
  font-size: 12px;
}

.drawer-actions {
  text-align: right;
}

/* 优化图表容器样式 */
.graph-canvas {
  cursor: grab;
  user-select: none;
}

/* 优化按钮样式 */
.graph-controls .ant-btn {
  border-radius: 4px;
  margin-right: 8px;
}
</style>
