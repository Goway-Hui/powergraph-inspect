package com.powergraph.inspect.service;

import com.powergraph.inspect.domain.Role;

import java.time.LocalDateTime;
import java.util.List;

public interface RoleService {
    List<Role> getAllRoles();
    Role createRole(Role role);
    Role updateRole(Long id, Role role);
    void deleteRole(Long id);

    List<Role> searchRoles(String keyword, String status, LocalDateTime start, LocalDateTime end);
    void updateRolesStatus(List<Long> ids, String status);
    void deleteRolesBatch(List<Long> ids);
}