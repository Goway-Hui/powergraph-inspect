package com.powergraph.inspect.controller;

import com.powergraph.inspect.service.UserService;
import com.powergraph.inspect.common.ApiResponse;
import com.powergraph.inspect.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/kpi")
    public ApiResponse<Object> getKpi() {
        // 模拟KPI数据
        Map<String, Object> kpiData = new HashMap<>();
        kpiData.put("deviceCount", 128);
        kpiData.put("normalRate", 96.8);
        kpiData.put("alertCount", 5);
        kpiData.put("taskCompletionRate", 87.3);
        
        return ApiResponse.success(kpiData);
    }
    
    @GetMapping("/users")
    public ApiResponse<List<User>> getAllUsers() {
        return userService.getAllUsers();
    }
    
    @PostMapping("/users")
    public ApiResponse<User> createUser(@RequestBody User user) {
        return userService.createUser(user);
    }
    
    @PutMapping("/users/{id}")
    public ApiResponse<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        return userService.updateUser(id, user);
    }
    
    @DeleteMapping("/users/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        return userService.deleteUser(id);
    }
}