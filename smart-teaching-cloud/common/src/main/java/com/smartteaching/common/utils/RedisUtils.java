package com.smartteaching.common.utils;

import com.alibaba.fastjson2.JSON;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * Redis工具类
 * <p>
 * 功能：封装RedisTemplate与StringRedisTemplate常用操作
 * 1. 对象操作使用redisTemplate（存POJO实体，JSON序列化）
 * 2. 纯字符串操作使用stringRedisTemplate（token、黑名单、验证码）
 *
 * @author SmartTeaching
 * @since 1.0.0
 */
@Component
public class RedisUtils {

    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    public RedisUtils(RedisTemplate<String, Object> redisTemplate, StringRedisTemplate stringRedisTemplate) {
        this.redisTemplate = redisTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    // ========= 对象读写（POJO实体） =========

    /**
     * 设置对象，带过期时间
     */
    public void setObject(String key, Object value, long expire, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, expire, unit);
    }

    /**
     * 获取对象
     */
    public Object getObject(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 获取对象并自动转换类型（泛型方法）
     */
    @SuppressWarnings("unchecked")
    public <T> T getObject(String key, Class<T> clazz) {
        Object obj = redisTemplate.opsForValue().get(key);
        if (obj == null) {
            return null;
        }
        // 如果存的是JSON字符串，需要反序列化；如果存的是对象，直接强转
        if (obj instanceof String) {
            return JSON.parseObject((String) obj, clazz);
        }
        if (clazz.isInstance(obj)) {
            return (T) obj;
        }
        // 兜底：尝试JSON转换
        return JSON.parseObject(JSON.toJSONString(obj), clazz);
    }

    // ========= 字符串读写（token、黑名单、验证码、缓存JSON） =========

    /**
     * 设置字符串，带过期时间
     */
    public void setStr(String key, String value, long expire, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, value, expire, unit);
    }

    /**
     * 获取字符串
     */
    public String getStr(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * 设置字符串（无过期时间）
     */
    public void setStr(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    // ========= 通用操作 =========

    /**
     * 删除key
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 批量删除key
     */
    public void deleteBatch(Collection<String> keys) {
        redisTemplate.delete(keys);
    }

    /**
     * 判断key是否存在
     */
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 设置key过期时间
     */
    public boolean expire(String key, long time, TimeUnit unit) {
        return Boolean.TRUE.equals(redisTemplate.expire(key, time, unit));
    }

    /**
     * 获取key的过期时间（剩余秒数）
     */
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 构建缓存Key（统一规范）
     */
    public static String buildKey(String prefix, Object... parts) {
        StringBuilder sb = new StringBuilder(prefix);
        for (Object part : parts) {
            sb.append(":").append(part);
        }
        return sb.toString();
    }
}