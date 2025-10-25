package com.powergraph.inspect.service.impl;

import com.powergraph.inspect.domain.Department;
import com.powergraph.inspect.repository.DepartmentRepository;
import com.powergraph.inspect.service.DepartmentService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Override
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    @Override
    public Department createDepartment(Department department) {
        return departmentRepository.save(department);
    }

    @Override
    public Department updateDepartment(Long id, Department department) {
        return departmentRepository.findById(id).map(existing -> {
            existing.setName(department.getName());
            existing.setParentId(department.getParentId());
            existing.setLeader(department.getLeader());
            if (department.getStatus() != null) {
                existing.setStatus(department.getStatus());
            }
            existing.setDescription(department.getDescription());
            return departmentRepository.save(existing);
        }).orElseThrow(() -> new RuntimeException("Department not found: " + id));
    }

    @Override
    public void deleteDepartment(Long id) {
        departmentRepository.deleteById(id);
    }

    @Override
    public List<Department> searchDepartments(String keyword, String status, Long parentId) {
        return departmentRepository.findAll().stream().filter(d -> {
            boolean matchKeyword = (keyword == null || keyword.isEmpty()) ||
                    d.getName().contains(keyword) || (d.getLeader() != null && d.getLeader().contains(keyword));
            boolean matchStatus = (status == null || status.isEmpty()) || Objects.equals(d.getStatus(), status);
            boolean matchParent = (parentId == null) || Objects.equals(d.getParentId(), parentId) || Objects.equals(d.getId(), parentId);
            return matchKeyword && matchStatus && matchParent;
        }).collect(Collectors.toList());
    }

    @Override
    public void updateDepartmentsStatus(List<Long> ids, String status) {
        List<Department> list = departmentRepository.findAllById(ids);
        for (Department d : list) {
            d.setStatus(status);
        }
        departmentRepository.saveAll(list);
    }

    @Override
    public void deleteDepartmentsBatch(List<Long> ids) {
        departmentRepository.deleteAllById(ids);
    }

    @Override
    public List<Map<String, Object>> getDepartmentTree() {
        List<Department> all = departmentRepository.findAll();
        Map<Long, Map<String, Object>> nodeMap = new HashMap<>();
        for (Department d : all) {
            Map<String, Object> node = new HashMap<>();
            node.put("key", d.getId());
            node.put("title", d.getName());
            node.put("children", new ArrayList<>());
            nodeMap.put(d.getId(), node);
        }
        List<Map<String, Object>> roots = new ArrayList<>();
        for (Department d : all) {
            Map<String, Object> node = nodeMap.get(d.getId());
            if (d.getParentId() == null) {
                roots.add(node);
            } else {
                Map<String, Object> parent = nodeMap.get(d.getParentId());
                if (parent != null) {
                    ((List<Map<String, Object>>) parent.get("children")).add(node);
                } else {
                    // 若父节点不存在（数据不一致），作为根处理
                    roots.add(node);
                }
            }
        }
        // 清理空children
        for (Map<String, Object> n : nodeMap.values()) {
            List<?> children = (List<?>) n.get("children");
            if (children != null && children.isEmpty()) {
                n.remove("children");
            }
        }
        return roots;
    }
}