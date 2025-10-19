package com.powergraph.inspect.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    // --- 关键依赖注入 ---

    /**
     * 1. 注入你自定义的JWT认证过滤器。
     * 这个过滤器需要你自行创建（如果你还没有的话），它负责从每个请求的 Authorization 头中解析Token、验证它，
     * 然后将用户信息加载到Spring Security的上下文中。
     * 这是解决403错误的核心。
     */
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 2. 注入认证失败的处理器。
     * 当一个未认证的请求访问受保护资源时，这个处理器会确保返回一个清晰的 401 Unauthorized 错误，
     * 而不是重定向到登录页面，这对于前后端分离的应用至关重要。
     */
    @Autowired
    private JwtAuthenticationEntryPoint unauthorizedHandler;

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                // 1. 禁用 CSRF 并配置 CORS
                .cors().and().csrf().disable()

                // 2. 配置异常处理，使用我们自定义的认证失败处理器
                .exceptionHandling().authenticationEntryPoint(unauthorizedHandler)
                .and()

                // 3. 设置会话管理为无状态(STATELESS)，因为我们使用JWT，不需要Session
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()

                // 4. 配置URL的授权规则
                .authorizeRequests()
                // 允许对登录接口(/auth/**)的匿名访问。
                // 注意：这里的路径是Vite代理转发后的路径，不包含 /api 前缀。
                .antMatchers("/auth/**").permitAll()

                // 其他所有接口，例如 /graph/**, /admin/** 等都需要认证后才能访问
                .antMatchers("/graph/**").authenticated()
                .antMatchers("/admin/**").hasRole("ADMIN")
                .antMatchers("/inspector/**").hasRole("INSPECTOR")
                .antMatchers("/viewer/**").hasRole("VIEWER")

                // 除了上面明确放行的，其他所有请求都需要认证
                .anyRequest().authenticated();

        // 5. 【最关键的一步】
        //    将我们自定义的JWT过滤器添加到Spring Security的过滤器链中。
        //    它必须在 UsernamePasswordAuthenticationFilter 之前执行，
        //    这样才能在Spring处理用户名密码认证之前，通过Token完成用户身份的验证。
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }
}