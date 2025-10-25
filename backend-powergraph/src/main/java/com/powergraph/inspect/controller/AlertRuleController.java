package com.powergraph.inspect.controller;

import com.powergraph.inspect.common.ApiResponse;
import com.powergraph.inspect.domain.AlertRule;
import com.powergraph.inspect.service.AlertRuleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/alert-rules")
public class AlertRuleController {

    private final AlertRuleService service;

    public AlertRuleController(AlertRuleService service) {
        this.service = service;
    }

    @GetMapping("/list")
    public ApiResponse<List<AlertRule>> list() {
        return ApiResponse.success(service.listAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<AlertRule> get(@PathVariable Long id) {
        return service.getById(id)
                .map(ApiResponse::success)
                .orElseGet(() -> ApiResponse.error("规则不存在: " + id));
    }

    @PostMapping("/create")
    public ApiResponse<AlertRule> create(@RequestBody AlertRule rule) {
        return ApiResponse.success(service.create(rule));
    }

    @PutMapping("/{id}")
    public ApiResponse<AlertRule> update(@PathVariable Long id, @RequestBody AlertRule rule) {
        try {
            return ApiResponse.success(service.update(id, rule));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/search")
    public ApiResponse<List<AlertRule>> search(@RequestBody Map<String, Object> body) {
        String keyword = body.get("keyword") == null ? null : body.get("keyword").toString();
        String level = body.get("level") == null ? null : body.get("level").toString();
        Boolean enabled = body.get("enabled") == null ? null : (Boolean) body.get("enabled");
        return ApiResponse.success(service.search(keyword, level, enabled));
    }

    @PostMapping("/batch/enable")
    public ApiResponse<Void> batchEnable(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked") List<Number> idNums = (List<Number>) body.get("ids");
        boolean enabled = body.get("enabled") != null && (Boolean) body.get("enabled");
        if (idNums == null || idNums.isEmpty()) {
            return ApiResponse.error("ids 不能为空");
        }
        List<Long> ids = idNums.stream().map(Number::longValue).collect(Collectors.toList());
        service.batchEnable(ids, enabled);
        return ApiResponse.success(null);
    }

    @PostMapping("/batch/delete")
    public ApiResponse<Void> batchDelete(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked") List<Number> idNums = (List<Number>) body.get("ids");
        if (idNums == null || idNums.isEmpty()) {
            return ApiResponse.error("ids 不能为空");
        }
        List<Long> ids = idNums.stream().map(Number::longValue).collect(Collectors.toList());
        service.batchDelete(ids);
        return ApiResponse.success(null);
    }
}