package com.smartteaching.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 配置类
 * <p>
 * 功能：
 * 1. 配置 RedisTemplate 的序列化方式（解决\xAC\xED乱码问题）
 * 2. 自定义JSON序列化器，避开Spring‑data‑redis4.x内部tools.jackson阴影包类型不匹配问题
 * 3. 序列化保存对象类型信息，反序列化还原原始POJO实体，避免得到LinkedHashMap
 * 4. 提供 StringRedisTemplate 用于简单 KV 字符串操作
 *
 * @author SmartTeaching
 * @since 1.0.0
 */
@Configuration
public class RedisConfig {

    /**
     * 自定义Jackson JSON序列化器
     * <p>
     * 解决Spring‑data‑redis4.x GenericJacksonJsonRedisSerializer强制依赖tools.jackson阴影包的类型不匹配问题
     * 序列化写入@class类型标识，反序列化可以直接得到原始实体对象
     */
    public static class CustomJacksonRedisSerializer implements RedisSerializer<Object> {

        private final ObjectMapper objectMapper;

        public CustomJacksonRedisSerializer(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        @Override
        public byte[] serialize(Object value) throws SerializationException {
            if (value == null) {
                return new byte[0];
            }
            try {
                return objectMapper.writeValueAsBytes(value);
            } catch (JsonProcessingException e) {
                throw new SerializationException("Redis序列化对象失败", e);
            }
        }

        @Override
        public Object deserialize(byte[] bytes) throws SerializationException {
            if (bytes == null || bytes.length == 0) {
                return null;
            }
            try {
                return objectMapper.readValue(bytes, Object.class);
            } catch (Exception e) {
                throw new SerializationException("Redis反序列化对象失败", e);
            }
        }
    }

    /**
     * 自定义Redis序列化使用的ObjectMapper
     * <p>
     * 配置说明：
     * - 设置所有成员字段可见，包含private私有字段
     * - 开启类型信息存储，JSON中写入@class，反序列化还原原始POJO实体
     *
     * @return 自定义配置的ObjectMapper实例
     */
    @Bean
    public ObjectMapper redisObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        // 设置可见性：所有字段（包括 private）都可序列化
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 启用类型信息存储（反序列化时能还原为具体实体类型，而非 LinkedHashMap）
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL
        );
        return objectMapper;
    }

    /**
     * 配置 RedisTemplate（通用模板，可存任意对象）
     * <p>
     * 序列化策略：
     * - key：使用 StringRedisSerializer（存为普通字符串，Redis客户端可读，避免\xAC\xED乱码）
     * - value：使用自定义CustomJacksonRedisSerializer（存为JSON格式，自带类型信息）
     * - hashKey：使用 StringRedisSerializer
     * - hashValue：使用自定义CustomJacksonRedisSerializer
     * <p>
     * 注意：Spring‑data‑redis4.x 内置GenericJacksonJsonRedisSerializer依赖tools.jackson阴影包，
     * 业务代码com.fasterxml.jackson.databind.ObjectMapper无法传入，因此采用自定义序列化器规避该问题。
     *
     * @param connectionFactory Redis 连接工厂
     * @param redisObjectMapper 自定义配置的ObjectMapper
     * @return 配置好的 RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory,
                                                       ObjectMapper redisObjectMapper) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 使用自定义JSON序列化器
        CustomJacksonRedisSerializer jacksonSerializer = new CustomJacksonRedisSerializer(redisObjectMapper);

        // 设置Key、HashKey序列化器为字符串序列化
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // 设置Value、HashValue序列化器为自定义JSON序列化
        template.setValueSerializer(jacksonSerializer);
        template.setHashValueSerializer(jacksonSerializer);

        // 加载属性配置，初始化RedisTemplate
        template.afterPropertiesSet();
        return template;
    }

    /**
     * 配置 StringRedisTemplate（专门用于纯字符串操作）
     * <p>
     * 适用场景：缓存简单的字符串值，如验证码、Token、计数器、黑名单token等
     * Spring Boot 原生已自动配置，此处显式声明便于统一管理
     *
     * @param connectionFactory Redis 连接工厂
     * @return StringRedisTemplate
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }
}
