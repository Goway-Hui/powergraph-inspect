package com.powergraph.inspect.controller;

import com.powergraph.inspect.common.ApiResponse;
import com.powergraph.inspect.service.GraphService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/graph")
public class GraphController {

    @Autowired
    private GraphService graphService;

    // --- 获取图谱全量数据 ---
    @GetMapping("/data")
    public ApiResponse<Object> getGraph() {
        try {
            Map<String, Object> graphData = graphService.getGraphData();
            return ApiResponse.success(graphData);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("获取图谱数据时发生内部错误: " + e.getMessage());
        }
    }

    // --- 节点操作 ---

    @PostMapping("/node")
    public ApiResponse<Object> addNode(@RequestBody Map<String, Object> nodeData) {
        try {
            Map<String, Object> created = graphService.addNode(nodeData);
            return ApiResponse.success(created);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("创建节点时发生错误: " + e.getMessage());
        }
    }

    @PutMapping("/node/{nodeId}")
    public ApiResponse<Object> updateNode(@PathVariable String nodeId, @RequestBody Map<String, Object> nodeData) {
        try {
            graphService.updateNode(nodeId, nodeData);
            return ApiResponse.success("节点更新成功");
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("更新节点时发生错误: " + e.getMessage());
        }
    }

    @DeleteMapping("/node/{nodeId}")
    public ApiResponse<Object> deleteNode(@PathVariable String nodeId) {
        try {
            graphService.deleteNode(nodeId);
            return ApiResponse.success("节点删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("删除节点时发生错误: " + e.getMessage());
        }
    }

    // --- 【新增】关系操作 ---

    /**
     * 创建新关系 (Edge)
     * @param edgeData 包含 source, target, relationship 等信息的 Map
     * @return 标准的 ApiResponse 响应
     */
    @PostMapping("/edge")
    public ApiResponse<Object> addEdge(@RequestBody Map<String, Object> edgeData) {
        try {
            graphService.addEdge(edgeData);
            return ApiResponse.success("关系创建成功");
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("创建关系时发生错误: " + e.getMessage());
        }
    }

    /**
     * 更新关系 (Edge) 的属性
     * @param edgeId 要更新的关系的 ID
     * @param edgeData 包含新属性的 Map
     * @return 标准的 ApiResponse 响应
     */
    @PutMapping("/edge/{edgeId}")
    public ApiResponse<Object> updateEdge(@PathVariable String edgeId, @RequestBody Map<String, Object> edgeData) {
        try {
            graphService.updateEdge(edgeId, edgeData);
            return ApiResponse.success("关系更新成功");
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("更新关系时发生错误: " + e.getMessage());
        }
    }

    /**
     * 删除关系 (Edge)
     * @param edgeId 要删除的关系的 ID
     * @return 标准的 ApiResponse 响应
     */
    @DeleteMapping("/edge/{edgeId}")
    public ApiResponse<Object> deleteEdge(@PathVariable String edgeId) {
        try {
            graphService.deleteEdge(edgeId);
            return ApiResponse.success("关系删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("删除关系时发生错误: " + e.getMessage());
        }
    }
}
