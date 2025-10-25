package com.powergraph.inspect.service.impl;

import com.powergraph.inspect.domain.Task;
import com.powergraph.inspect.repository.TaskRepository;
import com.powergraph.inspect.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Override
    public List<Task> listAll() {
        return taskRepository.findAll();
    }

    @Override
    public Task getById(Long id) {
        return taskRepository.findById(id).orElse(null);
    }

    @Override
    public Task create(Task task) {
        if (task.getStatus() == null) {
            task.setStatus("PENDING");
        }
        return taskRepository.save(task);
    }

    @Override
    public Task update(Long id, Task task) {
        return taskRepository.findById(id).map(existing -> {
            existing.setTitle(task.getTitle());
            existing.setDescription(task.getDescription());
            existing.setDeviceId(task.getDeviceId());
            existing.setAssigneeId(task.getAssigneeId());
            existing.setDepartmentId(task.getDepartmentId());
            existing.setCreatorId(task.getCreatorId());
            existing.setStationId(task.getStationId());
            existing.setTags(task.getTags());
            if (task.getStatus() != null) {
                existing.setStatus(task.getStatus());
            }
            existing.setPriority(task.getPriority());
            existing.setDeadline(task.getDeadline());
            existing.setCompletedAt(task.getCompletedAt());
            return taskRepository.save(existing);
        }).orElseThrow(() -> new RuntimeException("Task not found: " + id));
    }

    @Override
    public void delete(Long id) {
        taskRepository.deleteById(id);
    }

    @Override
    public List<Task> search(String keyword, String status, String priority,
                             Long assigneeId, Long deviceId, Long departmentId, Long stationId) {
        return taskRepository.findAll().stream()
                .filter(t -> keyword == null || keyword.isEmpty() ||
                        (t.getTitle() != null && t.getTitle().contains(keyword)) ||
                        (t.getDescription() != null && t.getDescription().contains(keyword)))
                .filter(t -> status == null || Objects.equals(t.getStatus(), status))
                .filter(t -> priority == null || Objects.equals(t.getPriority(), priority))
                .filter(t -> assigneeId == null || Objects.equals(t.getAssigneeId(), assigneeId))
                .filter(t -> deviceId == null || Objects.equals(t.getDeviceId(), deviceId))
                .filter(t -> departmentId == null || Objects.equals(t.getDepartmentId(), departmentId))
                .filter(t -> stationId == null || Objects.equals(t.getStationId(), stationId))
                .collect(Collectors.toList());
    }

    @Override
    public int batchUpdateStatus(List<Long> taskIds, String status) {
        if (taskIds == null || taskIds.isEmpty()) return 0;
        List<Task> tasks = taskRepository.findAllById(taskIds);
        tasks.forEach(t -> t.setStatus(status));
        taskRepository.saveAll(tasks);
        return tasks.size();
    }

    @Override
    public int batchDelete(List<Long> taskIds) {
        if (taskIds == null || taskIds.isEmpty()) return 0;
        taskRepository.deleteAll(taskRepository.findAllById(taskIds));
        return taskIds.size();
    }

    @Override
    public Task assign(Long id, Long assigneeId) {
        return taskRepository.findById(id).map(task -> {
            task.setAssigneeId(assigneeId);
            task.setStatus(task.getStatus() == null ? "PENDING" : task.getStatus());
            return taskRepository.save(task);
        }).orElseThrow(() -> new RuntimeException("Task not found: " + id));
    }

    @Override
    public Task markCompleted(Long id) {
        return taskRepository.findById(id).map(task -> {
            task.setStatus("COMPLETED");
            task.setCompletedAt(LocalDateTime.now());
            return taskRepository.save(task);
        }).orElseThrow(() -> new RuntimeException("Task not found: " + id));
    }
}