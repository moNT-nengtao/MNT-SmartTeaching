package com.smartteaching.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartteaching.entity.user.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 公共用户 Mapper：各服务通用基础 CRUD。
 * uaa 服务的自定义用户查询见 UserExtMapper（继承本接口）。
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
