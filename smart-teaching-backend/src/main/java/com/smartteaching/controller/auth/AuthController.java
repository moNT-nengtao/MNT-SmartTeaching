package com.smartteaching.controller.auth;

import com.smartteaching.common.dto.UserChangePasswordDTO;
import com.smartteaching.common.dto.UserLoginDTO;
import com.smartteaching.common.exception.TokenInvalidException;
import com.smartteaching.common.result.Result;
import com.smartteaching.common.utils.JwtUtil;
import com.smartteaching.common.vo.UserLoginVO;
import com.smartteaching.entity.user.User;
import com.smartteaching.service.auth.AuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户登录
     *
     * @param userLoginDTO 登录参数
     * @return token + 用户信息
     */
    @PostMapping("/login")
    public Result<UserLoginVO> login(@Valid @RequestBody UserLoginDTO userLoginDTO) {
        log.info("用户登录，参数：{}", userLoginDTO.getUsername());

        User user = authService.login(userLoginDTO);

        // 读取Redis令牌
        String redisKey = "user:token:version:" + user.getId();
        String versionStr = stringRedisTemplate.opsForValue().get(redisKey);
        Long tokenVersion;
        if (versionStr == null) {
            tokenVersion = 1L;
            stringRedisTemplate.opsForValue().set(redisKey, String.valueOf(tokenVersion));
        } else {
            tokenVersion = Long.parseLong(versionStr);
        }

        // 调用重载方法写入JWT载荷
        String token = jwtUtil.generateToken(user.getUsername(), user.getId(), tokenVersion);
        log.info("Token: {}", token);

        //组装
        UserLoginVO loginVO = new UserLoginVO();
        BeanUtils.copyProperties(user, loginVO);
        loginVO.setToken(token);

        return Result.success(loginVO);

    }

    /**
     * 获取当前登录用户信息
     *
     * @param authentication 认证信息
     * @return 用户信息
     */
    @GetMapping("/userInfo")
    public Result<UserLoginVO> getUserInfo(Authentication authentication) {
        // 从认证信息中获取用户名
        String username = authentication.getName();
        log.info("获取用户信息，用户名：{}", username);

        // 根据用户名查询用户信息
        User user = authService.getUserByUsername(username);

        // 组装VO
        UserLoginVO loginVO = new UserLoginVO();
        BeanUtils.copyProperties(user, loginVO);

        return Result.success(loginVO);
    }


    /**
     * 用户登出
     *
     * @param authorization
     * @return
     */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String authorization) {
        log.info("用户登出：{}", authorization);

        String token;
        //去前缀
        try {
            token = jwtUtil.extractToken(authorization);
        } catch (TokenInvalidException e) {
            log.warn("登出Authorization头格式异常");
            return Result.success(null);
        }
        //判断token状态
        try {
            long remainMs = jwtUtil.getTokenRemainExpireMs(token);
            if (remainMs > 0) {
                stringRedisTemplate.opsForValue().set("jwt:blacklist:" + token, "", remainMs, TimeUnit.MILLISECONDS);
            }
        } catch (TokenInvalidException e) {
            log.warn("登出token无效，无需拉黑");
        }
        log.info("用户执行登出操作");
        return Result.success(null);
    }


    /**
     * 修改密码
     *
     * @return
     */
    @PutMapping("/changePassword")
    public Result changePassword(@RequestBody UserChangePasswordDTO userChangePasswordDTO) {
        log.info("修改密码：{}", userChangePasswordDTO);
        authService.oldPasswordVerification(userChangePasswordDTO);
        return Result.success();
    }

}




