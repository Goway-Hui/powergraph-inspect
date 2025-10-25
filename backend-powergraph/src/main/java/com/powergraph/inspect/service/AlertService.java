package com.powergraph.inspect.service;

import com.powergraph.inspect.domain.Alert;

import java.util.List;
import java.util.Optional;

public interface AlertService {
    // 基础 CRUD
    List<Alert> listAll();
    Optional<Alert> getById(Long id);
    Alert create(Alert alert);
    Alert update(Long id, Alert alert);
    void delete(Long id);

    // 条件筛选
    List<Alert> search(
            String keyword,
            String status,
            String level,
            Long deviceId,
            Long assignedTo
    );

    // 批处理
    void batchUpdateStatus(List<Long> ids, String status);
    void batchDelete(List<Long> ids);

    // 业务操作
    Alert assign(Long id, Long userId);
    Alert markResolved(Long id);
}