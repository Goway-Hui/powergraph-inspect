package com.powergraph.inspect.repository;

import com.powergraph.inspect.domain.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByAssigneeId(Long assigneeId);
    List<Task> findByStatus(String status);
    List<Task> findByPriority(String priority);
}