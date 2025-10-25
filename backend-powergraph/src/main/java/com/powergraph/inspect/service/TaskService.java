package com.powergraph.inspect.service;

import com.powergraph.inspect.domain.Task;

import java.util.List;

public interface TaskService {
    // 基础 CRUD
    List<Task> listAll();
    Task getById(Long id);
    Task create(Task task);
    Task update(Long id, Task task);
    void delete(Long id);

    // 条件筛选
    List<Task> search(String keyword, String status, String priority,
                      Long assigneeId, Long deviceId, Long departmentId, Long stationId);

    // 批处理操作
    int batchUpdateStatus(List<Long> taskIds, String status);
    int batchDelete(List<Long> taskIds);

    // 业务操作
    Task assign(Long id, Long assigneeId);
    Task markCompleted(Long id);
}