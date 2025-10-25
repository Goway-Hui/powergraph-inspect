package com.powergraph.inspect.service;

import com.powergraph.inspect.domain.User;
import com.powergraph.inspect.common.ApiResponse;

import java.util.List;

public interface UserService {
    ApiResponse<User> createUser(User user);
    ApiResponse<User> getUserById(Long id);
    ApiResponse<User> getUserByUsername(String username);
    ApiResponse<List<User>> getAllUsers();
    ApiResponse<List<User>> searchUsers(String keyword, String role, String status, java.time.LocalDateTime start, java.time.LocalDateTime end);
    ApiResponse<User> updateUser(Long id, User user);
    ApiResponse<Void> deleteUser(Long id);
    ApiResponse<Void> updateUsersStatus(java.util.List<Long> ids, String status);
    ApiResponse<Void> deleteUsersBatch(java.util.List<Long> ids);
}