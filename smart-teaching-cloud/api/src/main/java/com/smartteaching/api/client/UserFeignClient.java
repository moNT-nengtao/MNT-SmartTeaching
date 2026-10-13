package com.smartteaching.api.client;

import com.smartteaching.api.dto.UserInfoVO;
import com.smartteaching.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 用户信息 Feign 客户端
 * 指向 uaa-service 的内部接口，供其它服务查询用户基础信息
 */
@FeignClient(name = "uaa-service", path = "/api/internal/user")
public interface UserFeignClient {

    /**
     * 按ID查询用户
     */
    @GetMapping("/{id}")
    Result<UserInfoVO> getUserById(@PathVariable("id") Long id);

    /**
     * 按用户名查询用户
     */
    @GetMapping("/byUsername/{username}")
    Result<UserInfoVO> getUserByUsername(@PathVariable("username") String username);
}
