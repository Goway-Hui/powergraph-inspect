package com.powergraph.inspect.service;

import com.powergraph.inspect.domain.User;
import com.powergraph.inspect.common.ApiResponse;

import java.util.List;
import java.util.Optional;

public interface UserService {
    ApiResponse<User> createUser(User user);
    ApiResponse<User> getUserById(Long id);
    ApiResponse<User> getUserByUsername(String username);
    ApiResponse<List<User>> getAllUsers();
    ApiResponse<User> updateUser(Long id, User user);
    ApiResponse<Void> deleteUser(Long id);
}