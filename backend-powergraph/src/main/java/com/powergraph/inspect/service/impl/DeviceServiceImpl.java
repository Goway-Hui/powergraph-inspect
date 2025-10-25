package com.powergraph.inspect.service.impl;

import com.powergraph.inspect.domain.Device;
import com.powergraph.inspect.repository.DeviceRepository;
import com.powergraph.inspect.service.DeviceService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;

    public DeviceServiceImpl(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    @Override
    public List<Device> getAllDevices() {
        return deviceRepository.findAll();
    }

    @Override
    public Device createDevice(Device device) {
        return deviceRepository.save(device);
    }

    @Override
    public Device updateDevice(Long id, Device device) {
        return deviceRepository.findById(id).map(existing -> {
            existing.setCode(device.getCode());
            existing.setName(device.getName());
            existing.setType(device.getType());
            existing.setModel(device.getModel());
            existing.setManufacturer(device.getManufacturer());
            existing.setStatus(device.getStatus());
            existing.setLocation(device.getLocation());
            existing.setStationId(device.getStationId());
            existing.setDepartmentId(device.getDepartmentId());
            existing.setInstallDate(device.getInstallDate());
            existing.setLastInspectionAt(device.getLastInspectionAt());
            existing.setMaintenanceCycleDays(device.getMaintenanceCycleDays());
            existing.setTags(device.getTags());
            return deviceRepository.save(existing);
        }).orElseThrow(() -> new RuntimeException("Device not found: " + id));
    }

    @Override
    public void deleteDevice(Long id) {
        deviceRepository.deleteById(id);
    }

    @Override
    public List<Device> searchDevices(String keyword, String type, String status, Long stationId, Long departmentId,
                                      LocalDateTime start, LocalDateTime end) {
        return deviceRepository.findAll().stream().filter(d -> {
            boolean matchKeyword = (keyword == null || keyword.isEmpty()) ||
                    (d.getName() != null && d.getName().contains(keyword)) ||
                    (d.getCode() != null && d.getCode().contains(keyword)) ||
                    (d.getModel() != null && d.getModel().contains(keyword)) ||
                    (d.getManufacturer() != null && d.getManufacturer().contains(keyword)) ||
                    (d.getLocation() != null && d.getLocation().contains(keyword));
            boolean matchType = (type == null || type.isEmpty()) || Objects.equals(d.getType(), type);
            boolean matchStatus = (status == null || status.isEmpty()) || Objects.equals(d.getStatus(), status);
            boolean matchStation = (stationId == null) || Objects.equals(d.getStationId(), stationId);
            boolean matchDept = (departmentId == null) || Objects.equals(d.getDepartmentId(), departmentId);
            boolean matchDate = true;
            if (start != null && d.getCreatedAt() != null) {
                matchDate = !d.getCreatedAt().isBefore(start);
            }
            if (matchDate && end != null && d.getCreatedAt() != null) {
                matchDate = !d.getCreatedAt().isAfter(end);
            }
            return matchKeyword && matchType && matchStatus && matchStation && matchDept && matchDate;
        }).collect(Collectors.toList());
    }

    @Override
    public void updateDevicesStatus(List<Long> ids, String status) {
        List<Device> list = deviceRepository.findAllById(ids);
        for (Device d : list) {
            d.setStatus(status);
        }
        deviceRepository.saveAll(list);
    }

    @Override
    public void deleteDevicesBatch(List<Long> ids) {
        deviceRepository.deleteAllById(ids);
    }
}