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
    public ApiResponse<List<User>> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime start,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime end
    ) {
        if (keyword != null || role != null || status != null || start != null || end != null) {
            return userService.searchUsers(keyword, role, status, start, end);
        }
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

    public static class BatchStatusRequest {
        public java.util.List<Long> ids;
        public String status;
    }

    @PostMapping("/users/batch/status")
    public ApiResponse<Void> updateUsersStatus(@RequestBody BatchStatusRequest req) {
        return userService.updateUsersStatus(req.ids, req.status);
    }

    public static class BatchDeleteRequest {
        public java.util.List<Long> ids;
    }

    @PostMapping("/users/batch/delete")
    public ApiResponse<Void> deleteUsersBatch(@RequestBody BatchDeleteRequest req) {
        return userService.deleteUsersBatch(req.ids);
    }
}