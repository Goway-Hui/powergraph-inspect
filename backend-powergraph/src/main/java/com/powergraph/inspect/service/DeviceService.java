package com.powergraph.inspect.service;

import com.powergraph.inspect.domain.Device;

import java.time.LocalDateTime;
import java.util.List;

public interface DeviceService {
    List<Device> getAllDevices();
    Device createDevice(Device device);
    Device updateDevice(Long id, Device device);
    void deleteDevice(Long id);

    List<Device> searchDevices(String keyword, String type, String status, Long stationId, Long departmentId,
                               LocalDateTime start, LocalDateTime end);

    void updateDevicesStatus(List<Long> ids, String status);
    void deleteDevicesBatch(List<Long> ids);
}