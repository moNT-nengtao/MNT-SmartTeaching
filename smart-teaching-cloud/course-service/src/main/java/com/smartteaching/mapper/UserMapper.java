package com.smartteaching.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartteaching.entity.user.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper（精简版，仅提供基础 CRUD）
 * 供 JWT 认证链路（loadUserByUsername / getCurrentUser）使用，
 * 用户域的列表/分页等能力统一由 uaa-service 提供（Feign 调用）
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
