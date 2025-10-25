package com.powergraph.inspect.repository;

import com.powergraph.inspect.domain.AlertRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRuleRepository extends JpaRepository<AlertRule, Long> {
    List<AlertRule> findByEnabled(boolean enabled);
    List<AlertRule> findByLevel(String level);
}