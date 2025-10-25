package com.powergraph.inspect.controller;

import com.powergraph.inspect.common.ApiResponse;
import com.powergraph.inspect.domain.Role;
import com.powergraph.inspect.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @GetMapping
    public ApiResponse<List<Role>> listRoles(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        if (keyword != null || status != null || start != null || end != null) {
            List<Role> list = roleService.searchRoles(keyword, status, start, end);
            return ApiResponse.success(list);
        }
        return ApiResponse.success(roleService.getAllRoles());
    }

    @PostMapping
    public ApiResponse<Role> createRole(@RequestBody Role role) {
        return ApiResponse.success(roleService.createRole(role));
    }

    @PutMapping("/{id}")
    public ApiResponse<Role> updateRole(@PathVariable Long id, @RequestBody Role role) {
        return ApiResponse.success(roleService.updateRole(id, role));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ApiResponse.success(null);
    }

    public static class BatchStatusRequest {
        public List<Long> ids;
        public String status;
    }

    @PostMapping("/batch/status")
    public ApiResponse<Void> updateRolesStatus(@RequestBody BatchStatusRequest req) {
        roleService.updateRolesStatus(req.ids, req.status);
        return ApiResponse.success(null);
    }

    public static class BatchDeleteRequest {
        public List<Long> ids;
    }

    @PostMapping("/batch/delete")
    public ApiResponse<Void> deleteRolesBatch(@RequestBody BatchDeleteRequest req) {
        roleService.deleteRolesBatch(req.ids);
        return ApiResponse.success(null);
    }
}