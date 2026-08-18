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
 * Redis配置
 * 解决redis乱码，自定义json序列化，提供两套模板
 */
@Configuration
public class RedisConfig {

    /**
     * 自定义JSON序列化器
     * 把对象转json存redis，带类型，取出来直接是实体，不是LinkedHashMap
     */
    public static class CustomJacksonRedisSerializer implements RedisSerializer<Object> {

        private final ObjectMapper objectMapper;

        public CustomJacksonRedisSerializer(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        // 对象转字节数组
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

        // 字节数组转回对象
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
     * 自定义Jackson对象映射
     * 所有字段都参与序列化，保存类类型信息
     */
    @Bean
    public ObjectMapper redisObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        // 私有字段也可以序列化
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 保存类型，反序列化还原原实体类
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL
        );
        return objectMapper;
    }

    /**
     * RedisTemplate：可以直接存Java对象
     * key用字符串序列化，value用自定义json序列化，避免\xAC\xED乱码
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory,
                                                       ObjectMapper redisObjectMapper) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        CustomJacksonRedisSerializer jacksonSerializer = new CustomJacksonRedisSerializer(redisObjectMapper);
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        // key、hash的key 使用字符串
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // value、hash的value 使用json序列化
        template.setValueSerializer(jacksonSerializer);
        template.setHashValueSerializer(jacksonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * StringRedisTemplate：只操作字符串
     * 适合token黑名单、版本号、验证码这类纯字符串KV
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }
}
