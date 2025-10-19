package com.powergraph.inspect.service; // 请确保包名与你的项目一致

import com.powergraph.inspect.common.ApiResponse;
import com.powergraph.inspect.domain.User; // 导入我们自己的 User 类
import com.powergraph.inspect.repository.UserRepository; // 导入 UserRepository
import com.powergraph.inspect.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    /**
     * --- 【核心修改 1】 ---
     * 注入 UserRepository，这样我们可以在认证成功后，获取完整的用户信息。
     */
    @Autowired
    private UserRepository userRepository;

    public ApiResponse<Object> login(String username, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String jwt = tokenProvider.generateToken(authentication);

            /**
             * --- 【核心修改 2】 ---
             * 不要直接返回 Spring Security 的 Principal 对象。
             * 而是根据已认证的用户名，从数据库中重新获取我们完整的 User 领域对象。
             * 这个对象才包含前端需要的 'role' 属性。
             */
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("认证通过，但在数据库中找不到用户，这是一个严重错误。"));

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("token", jwt);
            responseData.put("user", user); // <-- 现在返回的是包含 'role' 属性的完整 User 对象

            return ApiResponse.success(responseData);

        } catch (BadCredentialsException e) {
            return ApiResponse.error("用户名或密码无效");
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("登录时发生未知错误: " + e.getMessage());
        }
    }

    public ApiResponse<Object> logout(String token) {
        // 登出逻辑
        return ApiResponse.success("登出成功");
    }
}