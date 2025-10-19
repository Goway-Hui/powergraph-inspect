package com.powergraph.inspect.repository;

import com.powergraph.inspect.domain.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByDeviceId(Long deviceId);
    List<Alert> findByLevel(String level);
    List<Alert> findByStatus(String status);
    List<Alert> findByAssignedTo(Long assignedTo);
}