package com.powergraph.inspect.controller;

import com.powergraph.inspect.common.ApiResponse;
import com.powergraph.inspect.domain.Task;
import com.powergraph.inspect.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @GetMapping
    public ApiResponse<List<Task>> listTasks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) Long deviceId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long stationId
    ) {
        if (keyword != null || status != null || priority != null ||
                assigneeId != null || deviceId != null || departmentId != null || stationId != null) {
            return ApiResponse.success(
                    taskService.search(keyword, status, priority, assigneeId, deviceId, departmentId, stationId)
            );
        }
        return ApiResponse.success(taskService.listAll());
    }

    @PostMapping
    public ApiResponse<Task> createTask(@RequestBody Task task) {
        return ApiResponse.success(taskService.create(task));
    }

    @PutMapping("/{id}")
    public ApiResponse<Task> updateTask(@PathVariable Long id, @RequestBody Task task) {
        return ApiResponse.success(taskService.update(id, task));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteTask(@PathVariable Long id) {
        taskService.delete(id);
        return ApiResponse.success(null);
    }

    public static class BatchStatusRequest {
        public List<Long> ids;
        public String status;
    }

    @PostMapping("/batch/status")
    public ApiResponse<Void> updateTasksStatus(@RequestBody BatchStatusRequest req) {
        taskService.batchUpdateStatus(req.ids, req.status);
        return ApiResponse.success(null);
    }

    public static class BatchDeleteRequest {
        public List<Long> ids;
    }

    @PostMapping("/batch/delete")
    public ApiResponse<Void> deleteTasksBatch(@RequestBody BatchDeleteRequest req) {
        taskService.batchDelete(req.ids);
        return ApiResponse.success(null);
    }

    public static class AssignRequest {
        public Long id;
        public Long assigneeId;
    }

    @PostMapping("/assign")
    public ApiResponse<Task> assignTask(@RequestBody AssignRequest req) {
        return ApiResponse.success(taskService.assign(req.id, req.assigneeId));
    }

    @PostMapping("/{id}/complete")
    public ApiResponse<Task> completeTask(@PathVariable Long id) {
        return ApiResponse.success(taskService.markCompleted(id));
    }
}