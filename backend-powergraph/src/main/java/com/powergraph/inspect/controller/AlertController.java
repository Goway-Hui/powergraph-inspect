package com.powergraph.inspect.controller;

import com.powergraph.inspect.common.ApiResponse;
import com.powergraph.inspect.domain.Alert;
import com.powergraph.inspect.service.AlertService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping("/list")
    public ApiResponse<List<Alert>> list() {
        return ApiResponse.success(alertService.listAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<Alert> get(@PathVariable Long id) {
        return alertService.getById(id)
                .map(ApiResponse::success)
                .orElseGet(() -> ApiResponse.error("告警不存在: " + id));
    }

    @PostMapping("/create")
    public ApiResponse<Alert> create(@RequestBody Alert alert) {
        return ApiResponse.success(alertService.create(alert));
    }

    @PutMapping("/{id}")
    public ApiResponse<Alert> update(@PathVariable Long id, @RequestBody Alert alert) {
        try {
            return ApiResponse.success(alertService.update(id, alert));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        alertService.delete(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/search")
    public ApiResponse<List<Alert>> search(@RequestBody Map<String, Object> body) {
        String keyword = (String) body.getOrDefault("keyword", "");
        String status = (String) body.get("status");
        String level = (String) body.get("level");
        Long deviceId = body.get("deviceId") == null ? null : ((Number) body.get("deviceId")).longValue();
        Long assignedTo = body.get("assignedTo") == null ? null : ((Number) body.get("assignedTo")).longValue();
        return ApiResponse.success(alertService.search(keyword, status, level, deviceId, assignedTo));
    }

    @PostMapping("/batch/status")
    public ApiResponse<Void> batchUpdateStatus(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked") List<Number> idNums = (List<Number>) body.get("ids");
        String status = (String) body.get("status");
        if (idNums == null || idNums.isEmpty()) {
            return ApiResponse.error("ids 不能为空");
        }
        List<Long> ids = idNums.stream().map(Number::longValue).collect(Collectors.toList());
        alertService.batchUpdateStatus(ids, status);
        return ApiResponse.success(null);
    }

    @PostMapping("/batch/delete")
    public ApiResponse<Void> batchDelete(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked") List<Number> idNums = (List<Number>) body.get("ids");
        if (idNums == null || idNums.isEmpty()) {
            return ApiResponse.error("ids 不能为空");
        }
        List<Long> ids = idNums.stream().map(Number::longValue).collect(Collectors.toList());
        alertService.batchDelete(ids);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/assign")
    public ApiResponse<Alert> assign(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long userId = body.get("userId") == null ? null : ((Number) body.get("userId")).longValue();
        if (userId == null) {
            return ApiResponse.error("userId 不能为空");
        }
        try {
            return ApiResponse.success(alertService.assign(id, userId));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/resolve")
    public ApiResponse<Alert> resolve(@PathVariable Long id) {
        try {
            return ApiResponse.success(alertService.markResolved(id));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}