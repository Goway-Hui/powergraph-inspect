package com.powergraph.inspect.controller;

import com.powergraph.inspect.common.ApiResponse;
import com.powergraph.inspect.domain.Task;
import com.powergraph.inspect.domain.Alert;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inspector")
public class InspectorController {
    
    @GetMapping("/tasks/my")
    public ApiResponse<List<Task>> getMyTasks(@RequestHeader("Authorization") String token) {
        // 模拟获取当前用户任务
        List<Task> tasks = new ArrayList<>();
        // 在实际实现中，需要从token中解析用户信息并查询对应任务
        return ApiResponse.success(tasks);
    }
    
    @PostMapping("/alerts/report")
    public ApiResponse<Alert> reportAlert(@RequestBody Alert alert) {
        // 模拟上报告警
        return ApiResponse.success("告警上报成功", alert);
    }
    
    @GetMapping("/announcement/list")
    public ApiResponse<List<Map<String, Object>>> getAnnouncements() {
        // 模拟公告列表
        List<Map<String, Object>> announcements = new ArrayList<>();
        Map<String, Object> announcement1 = new HashMap<>();
        announcement1.put("id", 1);
        announcement1.put("title", "系统维护通知");
        announcement1.put("content", "本周六晚22:00-24:00进行系统维护，请提前安排好工作。");
        announcement1.put("type", "info");
        
        Map<String, Object> announcement2 = new HashMap<>();
        announcement2.put("id", 2);
        announcement2.put("title", "新功能上线");
        announcement2.put("content", "知识图谱查询功能已上线，可在相关页面使用。");
        announcement2.put("type", "success");
        
        Map<String, Object> announcement3 = new HashMap<>();
        announcement3.put("id", 3);
        announcement3.put("title", "安全提醒");
        announcement3.put("content", "请务必佩戴安全防护用品进行现场巡检。");
        announcement3.put("type", "warning");
        
        announcements.add(announcement1);
        announcements.add(announcement2);
        announcements.add(announcement3);
        
        return ApiResponse.success(announcements);
    }
}