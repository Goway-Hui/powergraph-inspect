package com.powergraph.inspect.controller;

import com.powergraph.inspect.common.ApiResponse;
import com.powergraph.inspect.domain.Device;
import com.powergraph.inspect.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/devices")
public class DeviceController {

    @Autowired
    private DeviceService deviceService;

    @GetMapping
    public ApiResponse<List<Device>> listDevices(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long stationId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        if (keyword != null || type != null || status != null || stationId != null || departmentId != null || start != null || end != null) {
            List<Device> list = deviceService.searchDevices(keyword, type, status, stationId, departmentId, start, end);
            return ApiResponse.success(list);
        }
        return ApiResponse.success(deviceService.getAllDevices());
    }

    @PostMapping
    public ApiResponse<Device> createDevice(@RequestBody Device device) {
        return ApiResponse.success(deviceService.createDevice(device));
    }

    @PutMapping("/{id}")
    public ApiResponse<Device> updateDevice(@PathVariable Long id, @RequestBody Device device) {
        return ApiResponse.success(deviceService.updateDevice(id, device));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteDevice(@PathVariable Long id) {
        deviceService.deleteDevice(id);
        return ApiResponse.success(null);
    }

    public static class BatchStatusRequest {
        public List<Long> ids;
        public String status;
    }

    @PostMapping("/batch/status")
    public ApiResponse<Void> updateDevicesStatus(@RequestBody BatchStatusRequest req) {
        deviceService.updateDevicesStatus(req.ids, req.status);
        return ApiResponse.success(null);
    }

    public static class BatchDeleteRequest {
        public List<Long> ids;
    }

    @PostMapping("/batch/delete")
    public ApiResponse<Void> deleteDevicesBatch(@RequestBody BatchDeleteRequest req) {
        deviceService.deleteDevicesBatch(req.ids);
        return ApiResponse.success(null);
    }
}