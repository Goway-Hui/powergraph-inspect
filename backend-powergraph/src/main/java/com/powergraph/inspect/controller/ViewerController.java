package com.powergraph.inspect.controller;

import com.powergraph.inspect.common.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/viewer")
public class ViewerController {
    
    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("deviceCount", 128);
        stats.put("normalRate", 96.8);
        stats.put("inspectionRate", 87.3);
        stats.put("alertCount", 5);
        return ApiResponse.success(stats);
    }
    
    @GetMapping("/alerts/realtime")
    public ApiResponse<List<Map<String, Object>>> getRealTimeAlerts() {
        List<Map<String, Object>> alerts = new ArrayList<>();
        
        Map<String, Object> alert1 = new HashMap<>();
        alert1.put("id", 1);
        alert1.put("deviceName", "变压器#001");
        alert1.put("level", "紧急");
        alert1.put("content", "温度过高");
        alert1.put("time", "2025-10-14 10:30:25");
        
        Map<String, Object> alert2 = new HashMap<>();
        alert2.put("id", 2);
        alert2.put("deviceName", "断路器#003");
        alert2.put("level", "重要");
        alert2.put("content", "操作次数超限");
        alert2.put("time", "2025-10-14 09:45:12");
        
        Map<String, Object> alert3 = new HashMap<>();
        alert3.put("id", 3);
        alert3.put("deviceName", "隔离开关#002");
        alert3.put("level", "一般");
        alert3.put("content", "机械部件磨损");
        alert3.put("time", "2025-10-14 08:22:47");
        
        alerts.add(alert1);
        alerts.add(alert2);
        alerts.add(alert3);
        
        return ApiResponse.success(alerts);
    }
}