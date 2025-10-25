package com.powergraph.inspect.domain;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "devices")
public class Device {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
    private String code;

    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String type;
    
    private String model;
    
    private String manufacturer;
    
    private String status;
    
    private String location;
    
    @Column(name = "station_id")
    private Long stationId;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "install_date")
    private LocalDate installDate;

    @Column(name = "last_inspection_at")
    private LocalDateTime lastInspectionAt;

    @Column(name = "maintenance_cycle_days")
    private Integer maintenanceCycleDays;

    // 存储为 JSON 字符串（可选）
    private String tags;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}