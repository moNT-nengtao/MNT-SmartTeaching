package com.smartteaching.controller.internal;

import com.smartteaching.api.dto.UserInfoVO;
import com.smartteaching.common.result.Result;
import com.smartteaching.entity.user.User;
import com.smartteaching.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户信息内部接口（仅服务间 Feign 调用）
 * 由 InternalApiCheckFilter 校验 X-Internal-Call 标识，外部无法直接访问
 */
@RestController
@RequestMapping("/api/internal/user")
public class UserInternalController {

    @Resource
    private UserMapper userMapper;

    /**
     * 按ID查询用户基础信息
     */
    @GetMapping("/{id}")
    public Result<UserInfoVO> getUserById(@PathVariable("id") Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success(toVO(user));
    }

    /**
     * 按用户名查询用户基础信息
     */
    @GetMapping("/byUsername/{username}")
    public Result<UserInfoVO> getUserByUsername(@PathVariable("username") String username) {
        User user = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username));
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success(toVO(user));
    }

    private UserInfoVO toVO(User user) {
        UserInfoVO vo = new UserInfoVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }
}
