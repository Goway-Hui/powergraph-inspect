// 将CSV数据转换为Cytoscape.js格式
export const convertToCytoscapeFormat = (nodes: any[], edges: any[]) => {
  // 转换节点
  const cytoscapeNodes = nodes.map(node => ({
    data: {
      id: node.id,
      name: node.name || node.title || node.description || node.label || node.id,
      ...node
    }
  }));

  // 转换边
  const cytoscapeEdges = edges.map(edge => ({
    data: {
      id: `${edge.source}-${edge.target}`,
      source: edge.source,
      target: edge.target,
      label: edge.relationship || edge.type || '',
      ...edge
    }
  }));

  return {
    nodes: cytoscapeNodes,
    edges: cytoscapeEdges
  };
};

// 根据节点类型设置样式
export const getNodeStyle = (nodeType: string) => {
  const styles: Record<string, any> = {
    station: {
      backgroundColor: '#165DFF',
      shape: 'rectangle',
      width: 60,
      height: 40
    },
    device: {
      backgroundColor: '#00B42A',
      shape: 'ellipse',
      width: 50,
      height: 50
    },
    task: {
      backgroundColor: '#FF7D00',
      shape: 'round-rectangle',
      width: 50,
      height: 30
    },
    alert: {
      backgroundColor: '#F53F3F',
      shape: 'diamond',
      width: 40,
      height: 40
    },
    person: {
      backgroundColor: '#7B68EE',
      shape: 'ellipse',
      width: 45,
      height: 45
    },
    document: {
      backgroundColor: '#FFB400',
      shape: 'round-rectangle',
      width: 50,
      height: 30
    }
  };

  return styles[nodeType] || {
    backgroundColor: '#888888',
    shape: 'ellipse',
    width: 40,
    height: 40
  };
};

// 根据关系类型设置样式
export const getEdgeStyle = (edgeType: string) => {
  const styles: Record<string, any> = {
    LOCATED_IN: {
      lineColor: '#165DFF',
      targetArrowColor: '#165DFF',
      targetArrowShape: 'triangle',
      curveStyle: 'bezier'
    },
    INSPECTS: {
      lineColor: '#00B42A',
      targetArrowColor: '#00B42A',
      targetArrowShape: 'triangle',
      curveStyle: 'bezier'
    },
    ASSIGNED_TO: {
      lineColor: '#FF7D00',
      targetArrowColor: '#FF7D00',
      targetArrowShape: 'triangle',
      curveStyle: 'bezier'
    },
    AFFECTS: {
      lineColor: '#F53F3F',
      targetArrowColor: '#F53F3F',
      targetArrowShape: 'triangle',
      curveStyle: 'bezier'
    },
    RELATED_TO: {
      lineColor: '#7B68EE',
      targetArrowColor: '#7B68EE',
      targetArrowShape: 'triangle',
      curveStyle: 'bezier'
    },
    DOCUMENTS: {
      lineColor: '#FFB400',
      targetArrowColor: '#FFB400',
      targetArrowShape: 'triangle',
      curveStyle: 'bezier'
    }
  };

  return styles[edgeType] || {
    lineColor: '#888888',
    targetArrowColor: '#888888',
    targetArrowShape: 'triangle',
    curveStyle: 'bezier'
  };
};