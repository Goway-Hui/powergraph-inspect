<template>
  <div class="preview-wrap">
    <div ref="canvas" class="preview-canvas"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, defineExpose } from 'vue';
import cytoscape from 'cytoscape';
import type { Core } from 'cytoscape';
import cola from 'cytoscape-cola';
import { getGraphData } from '../../api/graph';

// 注册布局扩展（如果已注册，多次调用也不会有问题）
try { cytoscape.use(cola as any); } catch {}

const canvas = ref<HTMLElement | null>(null);
let cy: Core | null = null;

const init = async () => {
  const res = await getGraphData();
  const nodes = (res?.data?.nodes || []).slice(0, 50); // 预览裁剪，避免过多元素影响性能
  const edges = (res?.data?.edges || []).slice(0, 80);

  const cytoscapeNodes = nodes.map((n: any) => ({ data: { id: n.id, name: n.name || n.label || n.id, ...n } }));
  const cytoscapeEdges = edges.map((e: any, idx: number) => ({
    data: {
      id: e.id || `${e.source}-${e.target}-${idx}`,
      source: e.source,
      target: e.target,
      label: e.relationship || e.type || ''
    }
  }));

  cy = cytoscape({
    container: canvas.value!,
    elements: { nodes: cytoscapeNodes, edges: cytoscapeEdges },
    style: [
      { selector: 'node', style: { 'background-color': '#00B42A', 'label': 'data(name)', 'color': '#fff', 'text-valign': 'center', 'text-halign': 'center', 'font-size': '11px', 'width': 40, 'height': 40, 'events': 'yes' } },
      { selector: 'edge', style: { 'width': 2, 'line-color': '#888', 'target-arrow-color': '#888', 'target-arrow-shape': 'triangle', 'curve-style': 'bezier', 'label': 'data(label)', 'font-size': '9px', 'text-background-color': '#ffffff', 'text-background-opacity': 1, 'events': 'yes' } }
    ],
    layout: { name: 'cola' }
  });

  try { cy.fit(); } catch {}
};

const zoomIn = () => { if (cy) cy.zoom(cy.zoom() * 1.2); };
const zoomOut = () => { if (cy) cy.zoom(cy.zoom() / 1.2); };
const fit = () => { if (cy) try { cy.fit(); } catch {} };
const relayout = () => { if (cy) { const l = cy.layout({ name: 'cola' }); l.run(); } };

defineExpose({ zoomIn, zoomOut, fit, relayout });

onMounted(() => { init(); });
onBeforeUnmount(() => { if (cy) { cy.destroy(); cy = null; } });
</script>

<style scoped>
.preview-wrap { height: 320px; }
.preview-canvas { height: 100%; width: 100%; background: #fff; border: 1px dashed #e5e6eb; border-radius: 8px; }
</style>