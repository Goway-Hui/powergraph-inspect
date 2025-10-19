package com.powergraph.inspect.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Date;
import javax.crypto.SecretKey;

/**
 * 1. 【重要】添加 @Component 注解
 * 这个注解告诉 Spring：“这个类是一个需要你来管理的组件（Bean）”。
 * 这样，在其他地方（比如 JwtAuthenticationFilter）使用 @Autowired 时，Spring 就能找到并注入它了。
 */
@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    // 2. 定义一个安全的密钥。
    // 这是一个基于HMAC-SHA256的最小安全长度密钥。
    // 【最佳实践】在实际项目中，这个密钥应该存储在配置文件（application.properties）中，而不是硬编码在代码里。
    private final SecretKey jwtSecret = Keys.secretKeyFor(SignatureAlgorithm.HS512);

    // 3. 定义Token的过期时间（单位：毫秒）。
    // 【最佳实践】这个值也应该存储在配置文件中。这里设置为1小时。
    private final int jwtExpirationInMs = 3600000;

    /**
     * 生成一个新的JWT
     *
     * @param authentication Spring Security的Authentication对象，包含了用户信息
     * @return 生成的Token字符串
     */
    public String generateToken(Authentication authentication) {
        // 从Authentication对象中获取用户名
        String username = authentication.getName();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        // 构建JWT
        return Jwts.builder()
                .setSubject(username) // 将用户名作为主题
                .setIssuedAt(new Date()) // 设置签发时间
                .setExpiration(expiryDate) // 设置过期时间
                .signWith(SignatureAlgorithm.HS512, jwtSecret) // 兼容旧版API写法
                // 使用密钥和HS512算法进行签名
                .compact();
    }

    /**
     * 从JWT中解析出用户名
     *
     * @param token Token字符串
     * @return 用户名
     */
    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    /**
     * 验证JWT的有效性
     *
     * @param authToken Token字符串
     * @return 如果有效则返回true，否则返回false
     */
    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException ex) {
            logger.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty.");
        }
        return false;
    }
}