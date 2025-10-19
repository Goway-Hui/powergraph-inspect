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
    public ApiResponse<User> updateUser(Long id, User user) {
        Optional<User> existingUser = userRepository.findById(id);
        if (existingUser.isPresent()) {
            User updatedUser = existingUser.get();
            updatedUser.setName(user.getName());
            updatedUser.setRole(user.getRole());
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


}