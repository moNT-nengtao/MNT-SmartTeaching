package com.smartteaching.common.utils;

import com.smartteaching.common.exception.TokenInvalidException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 *
 * <p>提供 Token 生成、解析、验证、提取等功能</p>
 *
 * <p><b>可用方法：</b></p>
 * <ul>
 *   <li>生成 Token：{@link #generateToken(String)}、{@link #generateToken(String, Long, Long)}</li>
 *   <li>解析 Token：{@link #getUsernameByToken(String)}、{@link #getUserIdByToken(String)}、{@link #getTokenVersionByToken(String)}</li>
 *   <li>验证 Token：{@link #validateToken(String)}</li>
 *   <li>从请求头提取：{@link #extractToken(String)}、{@link #extractAndValidateToken(String)}</li>
 *   <li>从请求头直接获取用户信息：{@link #getUserIdFromHeader(String)}、{@link #getUsernameFromHeader(String)}</li>
 *   <li>获取剩余过期时间：{@link #getTokenRemainExpireMs(String)}</li>
 * </ul>
 *
 * @author SmartTeaching
 * @since 1.0.0
 */
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtUtil {

    // ==================== 配置属性（从 yml 注入） ====================

    /**
     * JWT 签名密钥（至少 32 位字符，建议使用 Base64 编码的随机字符串）
     * 配置项：jwt.secret
     */
    private String secret;

    /**
     * Token 过期时间（单位：毫秒）
     * 配置项：jwt.expiration
     * 示例：604800000 = 7 天
     */
    private long expiration;

    /**
     * Token 存放的请求头名称
     * 配置项：jwt.header
     * 默认值：Authorization
     */
    private String header;

    /**
     * Token 前缀（Bearer 方案标准前缀）
     * 配置项：jwt.token-prefix
     * 默认值：Bearer （注意末尾有空格）
     */
    private String tokenPrefix;

    // ==================== 运行时生成对象 ====================

    /**
     * HMAC 签名密钥对象（由 secret 生成，线程安全）
     * 使用 @PostConstruct 在属性注入完成后初始化
     */
    private SecretKey secretKey;

    // ==================== 初始化方法 ====================

    /**
     * 对象属性全部注入完成后，初始化 HMAC 签名密钥
     */
    @PostConstruct
    public void initSecretKey() {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
    }

    // ==================== Token 提取方法 ====================

    /**
     * 从 Authorization 头中提取 Token（去掉 Bearer 前缀）
     *
     * @param authHeader Authorization 头内容
     * @return 纯净的 Token 字符串
     * @throws TokenInvalidException 如果 authHeader 为空或格式错误
     */
    public String extractToken(String authHeader) {
        if (authHeader == null || authHeader.trim().isEmpty()) {
            throw new TokenInvalidException("Authorization 头不能为空");
        }

        String prefix = getTokenPrefix();
        if (!authHeader.startsWith(prefix)) {
            throw new TokenInvalidException(
                    String.format("Authorization 头格式错误，应以 '%s' 开头", prefix)
            );
        }

        return authHeader.substring(prefix.length()).trim();
    }

    /**
     * 从 Authorization 头中提取并验证 Token
     *
     * @param authHeader Authorization 头内容
     * @return 有效的 Token 字符串
     * @throws TokenInvalidException 如果 Token 无效或过期
     */
    public String extractAndValidateToken(String authHeader) {
        String token = extractToken(authHeader);
        if (!validateToken(token)) {
            throw new TokenInvalidException("Token 无效或已过期，请重新登录");
        }
        return token;
    }

    /**
     * 从 Authorization 头获取用户ID
     *
     * @param authHeader Authorization 头内容
     * @return 用户ID
     * @throws TokenInvalidException 如果 Token 无效或过期
     */
    public Long getUserIdFromHeader(String authHeader) {
        String token = extractAndValidateToken(authHeader);
        return getUserIdByToken(token);
    }

    /**
     * 从 Authorization 头获取用户名
     *
     * @param authHeader Authorization 头内容
     * @return 用户名
     * @throws TokenInvalidException 如果 Token 无效或过期
     */
    public String getUsernameFromHeader(String authHeader) {
        String token = extractAndValidateToken(authHeader);
        return getUsernameByToken(token);
    }

    // ==================== 核心业务方法 ====================

    /**
     * 生成 JWT Token（旧接口，保留兼容）
     *
     * @param username 用户名（存入 Token 的 subject 字段，用于标识用户）
     * @return 生成的 JWT Token 字符串
     */
    public String generateToken(String username) {
        return generateToken(username, null, null);
    }

    /**
     * 【新】生成JWT，携带 userId 和 tokenVersion令牌版本号
     *
     * @param username     用户名
     * @param userId       用户主键ID
     * @param tokenVersion redis中的令牌版本号
     * @return jwt token
     */
    public String generateToken(String username, Long userId, Long tokenVersion) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", username);
        if (userId != null) {
            claims.put("userId", userId);
        }
        if (tokenVersion != null) {
            claims.put("tokenVersion", tokenVersion);
        }

        return Jwts.builder()
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(secretKey)
                .compact();
    }

    /**
     * 从 Token 中解析出用户名（subject）
     *
     * @param token JWT Token 字符串
     * @return 用户名
     * @throws TokenInvalidException 如果 Token 无效或解析失败
     */
    public String getUsernameByToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getSubject();
        } catch (JwtException e) {
            throw new TokenInvalidException("Token 解析失败：" + e.getMessage());
        }
    }

    /**
     * 从token获取用户ID
     *
     * @param token JWT Token 字符串
     * @return 用户ID
     * @throws TokenInvalidException 如果 Token 无效或解析失败
     */
    public Long getUserIdByToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.get("userId", Long.class);
        } catch (JwtException e) {
            throw new TokenInvalidException("Token 解析失败：" + e.getMessage());
        }
    }

    /**
     * 从token获取令牌版本号
     *
     * @param token JWT Token 字符串
     * @return 令牌版本号
     * @throws TokenInvalidException 如果 Token 无效或解析失败
     */
    public Long getTokenVersionByToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.get("tokenVersion", Long.class);
        } catch (JwtException e) {
            throw new TokenInvalidException("Token 解析失败：" + e.getMessage());
        }
    }

    /**
     * 解析 Token，获取所有载荷数据（Claims）
     *
     * @param token JWT Token 字符串
     * @return 载荷对象
     * @throws JwtException 如果 Token 无效或解析失败
     */
    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 验证 Token 是否合法（签名正确 & 未过期 & 格式正确）
     * <p>
     * 注意：此方法只校验token本身，**不校验redis版本号，版本号校验要在拦截器/过滤器额外做**
     *
     * @param token JWT Token 字符串
     * @return true = 合法有效，false = 无效（过期/格式错误/签名错误）
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (ExpiredJwtException e) {
            // Token 已过期
        } catch (MalformedJwtException e) {
            // Token 格式错误（被篡改或不是合法的 JWT 格式）
        } catch (UnsupportedJwtException e) {
            // 不支持的 JWT 格式
        } catch (IllegalArgumentException e) {
            // claims 为空或参数非法
        } catch (JwtException e) {
            // 签名错误、密钥不匹配等
        }
        return false;
    }

    /**
     * 获取token剩余过期毫秒时间，用于redis黑名单过期
     *
     * @param token JWT Token 字符串
     * @return 剩余毫秒数
     * @throws TokenInvalidException 如果 Token 无效或解析失败
     */
    public long getTokenRemainExpireMs(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().getTime() - System.currentTimeMillis();
        } catch (JwtException e) {
            throw new TokenInvalidException("Token 解析失败：" + e.getMessage());
        }
    }

    // ==================== Getter & Setter ====================

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpiration() {
        return expiration;
    }

    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getTokenPrefix() {
        return tokenPrefix != null ? tokenPrefix : "Bearer ";
    }

    public void setTokenPrefix(String tokenPrefix) {
        this.tokenPrefix = tokenPrefix;
    }

    public SecretKey getSecretKey() {
        return secretKey;
    }
}