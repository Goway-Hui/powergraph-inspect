package com.powergraph.inspect.service.impl; // 确保包名正确

import com.powergraph.inspect.domain.User;
import com.powergraph.inspect.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;

/**
 * 【核心】这是专门为 Spring Security 提供的用户认证服务。
 * 它实现了 UserDetailsService 接口，Spring Security 会自动找到并使用它。
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    /**
     * 这是 UserDetailsService 接口的核心方法。
     * 当用户尝试登录时，Spring Security 会调用这个方法，并传入用户名。
     * 我们的任务是：
     * 1. 根据用户名从数据库查询用户。
     * 2. 如果找不到，必须抛出 UsernameNotFoundException 异常。
     * 3. 如果找到了，将我们自己的 User 对象，转换成 Spring Security认识的 UserDetails 对象并返回。
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. 使用 UserRepository 从数据库中查找用户
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("用户不存在: " + username)
                );

        // 2. 获取用户的角色，并将其转换为 Spring Security 需要的 GrantedAuthority 集合
        //    【重要】Spring Security 的 hasRole() 方法会自动为你添加 "ROLE_" 前缀进行匹配，
        //    所以这里我们创建 SimpleGrantedAuthority 时，也最好加上 "ROLE_" 前缀，成为一种规范。
        Set<GrantedAuthority> authorities = Collections.singleton(
                new SimpleGrantedAuthority("ROLE_" + user.getRole())
        );

        // 3. 返回 Spring Security 内置的 UserDetails 实现类
        //    参数：用户名、加密后的密码、权限集合
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }
}