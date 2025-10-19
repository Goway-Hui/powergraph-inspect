package com.powergraph.inspect.service;

import java.util.Map;

public interface GraphService {
    Map<String, Object> getGraphData();
    Map<String, Object> addNode(Map<String, Object> nodeData);
    void updateNode(String nodeId, Map<String, Object> nodeData);
    void deleteNode(String nodeId);
    void addEdge(Map<String, Object> edgeData);
    void updateEdge(String edgeId, Map<String, Object> edgeData);
    void deleteEdge(String edgeId);
}
