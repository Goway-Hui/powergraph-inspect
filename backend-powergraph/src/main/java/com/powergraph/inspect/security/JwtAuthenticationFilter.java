package com.powergraph.inspect.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider tokenProvider; // 你的Token生成和验证工具类

    @Autowired
    @Lazy
    private UserDetailsService userDetailsService; // Spring用来根据用户名加载用户的服务

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 打印日志，方便调试
        System.out.println("====== [JwtAuthenticationFilter] 正在处理请求: " + request.getRequestURI() + " ======");

        try {
            // 1. 从请求中获取JWT
            String jwt = getJwtFromRequest(request);
            System.out.println("Authorization Header 中的 Token: " + (jwt == null ? "未找到" : "已找到"));

            // 2. 验证Token的有效性
            if (jwt != null && tokenProvider.validateToken(jwt)) {
                System.out.println("Token 验证成功!");

                // 3. 从Token中获取用户名
                String username = tokenProvider.getUsernameFromJWT(jwt);
                System.out.println("从 Token 中解析出的用户名: " + username);

                // 4. 根据用户名加载用户信息 (UserDetails)
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                System.out.println("根据用户名加载到的用户信息: " + userDetails.getUsername() + ", 权限: " + userDetails.getAuthorities());

                // 5. 创建一个已认证的 Authentication 对象
                // 参数：用户信息、密码(在这里不需要，所以是null)、权限列表
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                System.out.println("创建 Authentication 对象成功!");

                // 6. 将这个 Authentication 对象设置到 Spring Security 的安全上下文中
                // 这是最关键的一步，只有执行了这一步，Spring Security才会认为当前用户是已登录的
                SecurityContextHolder.getContext().setAuthentication(authentication);
                System.out.println("用户 '" + username + "' 已成功认证，已设置 Security Context。");

            } else {
                System.out.println("Token 验证失败或 Token 为空。");
            }
        } catch (Exception ex) {
            // 捕获所有可能的异常，例如签名错误、Token过期等，并打印错误信息
            System.err.println("!!!!!! [JwtAuthenticationFilter] 认证过程中发生错误: " + ex.getMessage());
        }

        // 7. 无论认证成功与否，都必须继续执行过滤器链，让请求继续下去
        filterChain.doFilter(request, response);
        System.out.println("====== [JwtAuthenticationFilter] 请求处理完毕 ======");
    }

    /**
     * 一个辅助方法，用于从请求头的 "Authorization" 中提取 "Bearer Token"
     * @param request 当前的HTTP请求
     * @return 提取出的Token字符串，如果不存在则返回null
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        // 从请求头获取 "Authorization" 的值
        String bearerToken = request.getHeader("Authorization");
        // 检查它是否不为空，并且是否以 "Bearer " 开头
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // 如果是，则截取 "Bearer " 后面的部分，即真正的Token内容
            return bearerToken.substring(7);
        }
        return null;
    }
}
