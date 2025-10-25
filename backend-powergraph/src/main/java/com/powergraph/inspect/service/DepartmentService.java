package com.powergraph.inspect.service;

import com.powergraph.inspect.domain.Department;

import java.util.List;
import java.util.Map;

public interface DepartmentService {
    List<Department> getAllDepartments();
    Department createDepartment(Department department);
    Department updateDepartment(Long id, Department department);
    void deleteDepartment(Long id);

    List<Department> searchDepartments(String keyword, String status, Long parentId);
    void updateDepartmentsStatus(List<Long> ids, String status);
    void deleteDepartmentsBatch(List<Long> ids);

    List<Map<String, Object>> getDepartmentTree();
}