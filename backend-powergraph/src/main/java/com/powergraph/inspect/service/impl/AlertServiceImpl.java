package com.powergraph.inspect.service.impl;

import com.powergraph.inspect.domain.Alert;
import com.powergraph.inspect.repository.AlertRepository;
import com.powergraph.inspect.service.AlertService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AlertServiceImpl implements AlertService {

    private final AlertRepository alertRepository;

    public AlertServiceImpl(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    @Override
    public List<Alert> listAll() {
        return alertRepository.findAll();
    }

    @Override
    public Optional<Alert> getById(Long id) {
        return alertRepository.findById(id);
    }

    @Override
    public Alert create(Alert alert) {
        alert.setId(null);
        alert.setCreatedAt(LocalDateTime.now());
        alert.setUpdatedAt(LocalDateTime.now());
        return alertRepository.save(alert);
    }

    @Override
    public Alert update(Long id, Alert update) {
        return alertRepository.findById(id).map(existing -> {
            existing.setDeviceId(update.getDeviceId());
            existing.setLevel(update.getLevel());
            existing.setContent(update.getContent());
            existing.setStatus(update.getStatus());
            existing.setAssignedTo(update.getAssignedTo());
            existing.setResolvedAt(update.getResolvedAt());
            existing.setUpdatedAt(LocalDateTime.now());
            return alertRepository.save(existing);
        }).orElseThrow(() -> new RuntimeException("Alert not found: " + id));
    }

    @Override
    public void delete(Long id) {
        alertRepository.deleteById(id);
    }

    @Override
    public List<Alert> search(String keyword, String status, String level, Long deviceId, Long assignedTo) {
        List<Alert> all = alertRepository.findAll();
        return all.stream().filter(a -> {
            boolean ok = true;
            if (keyword != null && !keyword.isEmpty()) {
                String k = keyword.toLowerCase();
                String content = a.getContent() == null ? "" : a.getContent().toLowerCase();
                ok &= content.contains(k);
            }
            if (status != null && !status.isEmpty()) {
                ok &= status.equals(a.getStatus());
            }
            if (level != null && !level.isEmpty()) {
                ok &= level.equals(a.getLevel());
            }
            if (deviceId != null) {
                ok &= deviceId.equals(a.getDeviceId());
            }
            if (assignedTo != null) {
                ok &= assignedTo.equals(a.getAssignedTo());
            }
            return ok;
        }).collect(Collectors.toList());
    }

    @Override
    public void batchUpdateStatus(List<Long> ids, String status) {
        if (ids == null || ids.isEmpty()) return;
        List<Alert> list = alertRepository.findAllById(ids);
        for (Alert a : list) {
            a.setStatus(status);
            a.setUpdatedAt(LocalDateTime.now());
        }
        alertRepository.saveAll(list);
    }

    @Override
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        alertRepository.deleteAllById(ids);
    }

    @Override
    public Alert assign(Long id, Long userId) {
        return alertRepository.findById(id).map(a -> {
            a.setAssignedTo(userId);
            a.setUpdatedAt(LocalDateTime.now());
            return alertRepository.save(a);
        }).orElseThrow(() -> new RuntimeException("Alert not found: " + id));
    }

    @Override
    public Alert markResolved(Long id) {
        return alertRepository.findById(id).map(a -> {
            a.setStatus("已处理");
            a.setResolvedAt(LocalDateTime.now());
            a.setUpdatedAt(LocalDateTime.now());
            return alertRepository.save(a);
        }).orElseThrow(() -> new RuntimeException("Alert not found: " + id));
    }
}