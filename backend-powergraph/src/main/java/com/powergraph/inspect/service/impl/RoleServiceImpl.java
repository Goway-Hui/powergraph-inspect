package com.powergraph.inspect.service.impl;

import com.powergraph.inspect.domain.Role;
import com.powergraph.inspect.repository.RoleRepository;
import com.powergraph.inspect.service.RoleService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Override
    public Role createRole(Role role) {
        return roleRepository.save(role);
    }

    @Override
    public Role updateRole(Long id, Role role) {
        return roleRepository.findById(id).map(existing -> {
            existing.setName(role.getName());
            existing.setCode(role.getCode());
            existing.setDescription(role.getDescription());
            if (role.getStatus() != null) {
                existing.setStatus(role.getStatus());
            }
            existing.setPermissions(role.getPermissions());
            return roleRepository.save(existing);
        }).orElseThrow(() -> new RuntimeException("Role not found: " + id));
    }

    @Override
    public void deleteRole(Long id) {
        roleRepository.deleteById(id);
    }

    @Override
    public List<Role> searchRoles(String keyword, String status, LocalDateTime start, LocalDateTime end) {
        return roleRepository.findAll().stream().filter(r -> {
            boolean matchKeyword = (keyword == null || keyword.isEmpty()) ||
                    r.getName().contains(keyword) || r.getCode().contains(keyword) ||
                    (r.getDescription() != null && r.getDescription().contains(keyword));
            boolean matchStatus = (status == null || status.isEmpty()) || Objects.equals(r.getStatus(), status);
            boolean matchDate = true;
            if (start != null && end != null && r.getCreatedAt() != null) {
                matchDate = !r.getCreatedAt().isBefore(start) && !r.getCreatedAt().isAfter(end);
            }
            return matchKeyword && matchStatus && matchDate;
        }).collect(Collectors.toList());
    }

    @Override
    public void updateRolesStatus(List<Long> ids, String status) {
        List<Role> roles = roleRepository.findAllById(ids);
        for (Role r : roles) {
            r.setStatus(status);
        }
        roleRepository.saveAll(roles);
    }

    @Override
    public void deleteRolesBatch(List<Long> ids) {
        roleRepository.deleteAllById(ids);
    }
}