package com.powergraph.inspect.controller;

import com.powergraph.inspect.common.ApiResponse;
import com.powergraph.inspect.domain.Department;
import com.powergraph.inspect.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/departments")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @GetMapping
    public ApiResponse<List<Department>> listDepartments(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long parentId
    ) {
        if (keyword != null || status != null || parentId != null) {
            List<Department> list = departmentService.searchDepartments(keyword, status, parentId);
            return ApiResponse.success(list);
        }
        return ApiResponse.success(departmentService.getAllDepartments());
    }

    @GetMapping("/tree")
    public ApiResponse<List<Map<String, Object>>> getTree() {
        return ApiResponse.success(departmentService.getDepartmentTree());
    }

    @PostMapping
    public ApiResponse<Department> createDepartment(@RequestBody Department department) {
        return ApiResponse.success(departmentService.createDepartment(department));
    }

    @PutMapping("/{id}")
    public ApiResponse<Department> updateDepartment(@PathVariable Long id, @RequestBody Department department) {
        return ApiResponse.success(departmentService.updateDepartment(id, department));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ApiResponse.success(null);
    }

    public static class BatchStatusRequest {
        public List<Long> ids;
        public String status;
    }

    @PostMapping("/batch/status")
    public ApiResponse<Void> updateDepartmentsStatus(@RequestBody BatchStatusRequest req) {
        departmentService.updateDepartmentsStatus(req.ids, req.status);
        return ApiResponse.success(null);
    }

    public static class BatchDeleteRequest {
        public List<Long> ids;
    }

    @PostMapping("/batch/delete")
    public ApiResponse<Void> deleteDepartmentsBatch(@RequestBody BatchDeleteRequest req) {
        departmentService.deleteDepartmentsBatch(req.ids);
        return ApiResponse.success(null);
    }
}