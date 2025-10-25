package com.powergraph.inspect.service;

import com.powergraph.inspect.domain.AlertRule;

import java.util.List;
import java.util.Optional;

public interface AlertRuleService {
    List<AlertRule> listAll();
    Optional<AlertRule> getById(Long id);
    AlertRule create(AlertRule rule);
    AlertRule update(Long id, AlertRule rule);
    void delete(Long id);

    List<AlertRule> search(String keyword, String level, Boolean enabled);

    void batchEnable(List<Long> ids, boolean enabled);
    void batchDelete(List<Long> ids);
}