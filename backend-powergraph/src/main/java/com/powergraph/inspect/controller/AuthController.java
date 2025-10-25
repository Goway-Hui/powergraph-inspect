package com.powergraph.inspect.controller;

import com.powergraph.inspect.service.AuthService;
import com.powergraph.inspect.common.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
// --- 修改在这里 ---
// 移除 /api 前缀，以匹配 Vite 代理重写后的路径
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ApiResponse<Object> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        if (username == null || password == null) {
            return ApiResponse.error("用户名和密码不能为空");
        }

        return authService.login(username, password);
    }

    @PostMapping("/logout")
    public ApiResponse<Object> logout(@RequestHeader("Authorization") String token) {
        // 移除Bearer前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return authService.logout(token);
    }
}