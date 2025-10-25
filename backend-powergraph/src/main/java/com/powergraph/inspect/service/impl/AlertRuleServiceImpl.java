package com.powergraph.inspect.service.impl;

import com.powergraph.inspect.domain.AlertRule;
import com.powergraph.inspect.repository.AlertRuleRepository;
import com.powergraph.inspect.service.AlertRuleService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AlertRuleServiceImpl implements AlertRuleService {

    private final AlertRuleRepository repository;

    public AlertRuleServiceImpl(AlertRuleRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<AlertRule> listAll() {
        return repository.findAll();
    }

    @Override
    public Optional<AlertRule> getById(Long id) {
        return repository.findById(id);
    }

    @Override
    public AlertRule create(AlertRule rule) {
        rule.setId(null);
        rule.setCreatedAt(LocalDateTime.now());
        rule.setUpdatedAt(LocalDateTime.now());
        return repository.save(rule);
    }

    @Override
    public AlertRule update(Long id, AlertRule update) {
        return repository.findById(id).map(existing -> {
            existing.setName(update.getName());
            existing.setLevel(update.getLevel());
            existing.setCondition(update.getCondition());
            existing.setScope(update.getScope());
            existing.setNotifyChannels(update.getNotifyChannels());
            existing.setEnabled(update.isEnabled());
            existing.setUpdatedAt(LocalDateTime.now());
            return repository.save(existing);
        }).orElseThrow(() -> new RuntimeException("AlertRule not found: " + id));
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    public List<AlertRule> search(String keyword, String level, Boolean enabled) {
        List<AlertRule> all = repository.findAll();
        return all.stream().filter(r -> {
            boolean ok = true;
            if (keyword != null && !keyword.isEmpty()) {
                String k = keyword.toLowerCase();
                String name = r.getName() == null ? "" : r.getName().toLowerCase();
                String cond = r.getCondition() == null ? "" : r.getCondition().toLowerCase();
                ok &= (name.contains(k) || cond.contains(k));
            }
            if (level != null && !level.isEmpty()) {
                ok &= level.equals(r.getLevel());
            }
            if (enabled != null) {
                ok &= enabled.equals(r.isEnabled());
            }
            return ok;
        }).collect(Collectors.toList());
    }

    @Override
    public void batchEnable(List<Long> ids, boolean enabled) {
        if (ids == null || ids.isEmpty()) return;
        List<AlertRule> list = repository.findAllById(ids);
        for (AlertRule r : list) {
            r.setEnabled(enabled);
            r.setUpdatedAt(LocalDateTime.now());
        }
        repository.saveAll(list);
    }

    @Override
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        repository.deleteAllById(ids);
    }
}