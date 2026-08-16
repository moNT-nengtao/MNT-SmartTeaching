package com.smartteaching.common.utils;

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
 * JWT（JSON Web Token）工具类
 * <p>
 * 功能：生成 Token、解析 Token、验证 Token 合法性，支持token版本号作废
 * <p>
 * 配置来源：application.yml 中的 jwt.* 配置项
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
     * <p>
     * 执行时机：Spring 容器完成依赖注入后自动调用
     * 作用：将字符串密钥转换为 JWT 库所需的 SecretKey 对象
     */
    @PostConstruct
    public void initSecretKey() {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
    }

    // ==================== 核心业务方法 ====================

    /**
     * 生成 JWT Token（旧接口，保留兼容）
     *
     * @param username 用户名（存入 Token 的 subject 字段，用于标识用户）
     * @return 生成的 JWT Token 字符串
     */
    public String generateToken(String username) {
        return generateToken(username,null,null);
    }

    /**
     * 【新】生成JWT，携带 userId 和 tokenVersion令牌版本号
     * @param username 用户名
     * @param userId 用户主键ID
     * @param tokenVersion redis中的令牌版本号
     * @return jwt token
     */
    public String generateToken(String username, Long userId, Long tokenVersion) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", username);
        if(userId != null){
            claims.put("userId", userId);
        }
        if(tokenVersion != null){
            claims.put("tokenVersion", tokenVersion);
        }

        return Jwts.builder()
                .claims(claims)                       // 设置载荷（携带的用户信息）
                .issuedAt(new Date())                 // 签发时间
                .expiration(new Date(System.currentTimeMillis() + expiration)) // 过期时间
                .signWith(secretKey)                  // 使用 HMAC 密钥签名
                .compact();                           // 压缩为最终 Token 字符串
    }


    /**
     * 从 Token 中解析出用户名（subject）
     *
     * @param token JWT Token 字符串
     * @return 用户名
     */
    public String getUsernameByToken(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /**
     * 从token获取用户ID
     */
    public Long getUserIdByToken(String token){
        Claims claims = parseToken(token);
        return claims.get("userId",Long.class);
    }

    /**
     * 从token获取令牌版本号
     */
    public Long getTokenVersionByToken(String token){
        Claims claims = parseToken(token);
        return claims.get("tokenVersion",Long.class);
    }

    /**
     * 解析 Token，获取所有载荷数据（Claims）
     * <p>
     * Claims 中可包含：subject(用户名)、userId、tokenVersion、issuedAt(签发时间)、expiration(过期时间) 等
     *
     * @param token JWT Token 字符串
     * @return 载荷对象
     */
    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)                // 使用同一密钥验证签名
                .build()
                .parseSignedClaims(token)             // 解析并验证签名
                .getPayload();                        // 获取载荷部分
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
            parseToken(token);  // 能正常解析说明 Token 合法
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

    // 获取token剩余过期毫秒时间，用于redis黑名单过期
    public long getTokenRemainExpireMs(String token){
        Claims claims = parseToken(token);
        return claims.getExpiration().getTime() - System.currentTimeMillis();
    }

    // ==================== Getter & Setter ====================
    // 必须要有！@ConfigurationProperties 通过 setter 方法反射注入配置值
    // 如果没有 setter，Spring Boot 无法将 yml 中的值绑定到这些字段

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
        return tokenPrefix;
    }

    public void setTokenPrefix(String tokenPrefix) {
        this.tokenPrefix = tokenPrefix;
    }

    public SecretKey getSecretKey() {
        return secretKey;
    }
}
