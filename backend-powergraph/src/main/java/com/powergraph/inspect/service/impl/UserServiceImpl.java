package com.powergraph.inspect.service.impl;

import com.powergraph.inspect.domain.User;
import com.powergraph.inspect.repository.UserRepository;
import com.powergraph.inspect.service.UserService;
import com.powergraph.inspect.common.ApiResponse;
import com.powergraph.inspect.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public ApiResponse<User> createUser(User user) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(user.getUsername())) {
            return ApiResponse.error("用户名已存在");
        }

        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 保存用户
        User savedUser = userRepository.save(user);
        return ApiResponse.success("用户创建成功", savedUser);
    }

    @Override
    public ApiResponse<User> getUserById(Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            return ApiResponse.success(user.get());
        } else {
            return ApiResponse.error("用户不存在");
        }
    }

    @Override
    public ApiResponse<User> getUserByUsername(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            return ApiResponse.success(user.get());
        } else {
            return ApiResponse.error("用户不存在");
        }
    }

    @Override
    public ApiResponse<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ApiResponse.success(users);
    }

    @Override
    public ApiResponse<List<User>> searchUsers(String keyword, String role, String status, java.time.LocalDateTime start, java.time.LocalDateTime end) {
        List<User> all = userRepository.findAll();
        List<User> filtered = all.stream().filter(u -> {
            boolean matchKeyword = (keyword == null || keyword.isEmpty()) ||
                    (u.getName() != null && u.getName().contains(keyword)) ||
                    (u.getUsername() != null && u.getUsername().contains(keyword));
            boolean matchRole = (role == null || role.isEmpty()) || role.equalsIgnoreCase(u.getRole());
            boolean matchStatus = (status == null || status.isEmpty()) || status.equalsIgnoreCase(u.getStatus());
            boolean matchDate = true;
            if (start != null) {
                matchDate = u.getCreatedAt() != null && !u.getCreatedAt().isBefore(start);
            }
            if (matchDate && end != null) {
                matchDate = u.getCreatedAt() != null && !u.getCreatedAt().isAfter(end);
            }
            return matchKeyword && matchRole && matchStatus && matchDate;
        }).collect(java.util.stream.Collectors.toList());
        return ApiResponse.success(filtered);
    }

    @Override
    public ApiResponse<User> updateUser(Long id, User user) {
        Optional<User> existingUser = userRepository.findById(id);
        if (existingUser.isPresent()) {
            User updatedUser = existingUser.get();
            updatedUser.setName(user.getName());
            updatedUser.setRole(user.getRole());
            updatedUser.setStatus(user.getStatus() != null ? user.getStatus() : updatedUser.getStatus());
            updatedUser.setAvatar(user.getAvatar());
            userRepository.save(updatedUser);
            return ApiResponse.success("用户更新成功", updatedUser);
        } else {
            return ApiResponse.error("用户不存在");
        }
    }

    @Override
    public ApiResponse<Void> deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return ApiResponse.success("用户删除成功", null);
        } else {
            return ApiResponse.error("用户不存在");
        }
    }

    @Override
    public ApiResponse<Void> updateUsersStatus(java.util.List<Long> ids, String status) {
        if (ids == null || ids.isEmpty()) {
            return ApiResponse.error("未提供用户ID");
        }
        List<User> users = userRepository.findAllById(ids);
        users.forEach(u -> u.setStatus(status));
        userRepository.saveAll(users);
        return ApiResponse.success("批量更新状态成功", null);
    }

    @Override
    public ApiResponse<Void> deleteUsersBatch(java.util.List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return ApiResponse.error("未提供用户ID");
        }
        userRepository.deleteAllById(ids);
        return ApiResponse.success("批量删除成功", null);
    }

}