package com.smartteaching.controller.auth;

import com.smartteaching.common.dto.UserChangePasswordDTO;
import com.smartteaching.common.dto.UserLoginDTO;
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

        //校验账号密码，拿到用户VO
        User user = authService.login(userLoginDTO);

        // ========== 读取Redis令牌版本号 ==========
        String redisKey = "user:token:version:" + user.getId();
        String versionStr = stringRedisTemplate.opsForValue().get(redisKey);
        Long tokenVersion;
        if (versionStr == null) {
            tokenVersion = 1L;
            stringRedisTemplate.opsForValue().set(redisKey, String.valueOf(tokenVersion));
        } else {
            tokenVersion = Long.parseLong(versionStr);
        }

        // 调用重载方法，把 username、userId、tokenVersion 全部写入JWT载荷
        String token = jwtUtil.generateToken(user.getUsername(), user.getId(), tokenVersion);

        //组装VO，复制属性
        UserLoginVO loginVO = new UserLoginVO();
        BeanUtils.copyProperties(user, loginVO);
        loginVO.setToken(token);

        //loginVO里面带了token
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
        // 注意：这里不需要再生成token

        return Result.success(loginVO);
    }


    /**
     * 用户登出
     *
     * @param authorization
     * @return
     */
//    第一步：修改 JwtUtil，增加获取 token 过期时间方法
//    第二步：AuthController 添加 logout 接口
//    第三步：修改 JWT 过滤器，增加黑名单校验（核心！）
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String authorization) {
        log.info("用户登出：{}", authorization);

        //去掉Bearer前缀
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            //获取token剩余有效期
            long remainMs = jwtUtil.getTokenRemainExpireMs(token);
            if (remainMs > 0) {
                //放入黑名单，key: jwt:blacklist:{token}
                stringRedisTemplate.opsForValue().set("jwt:blacklist:" + token, "", remainMs, TimeUnit.MILLISECONDS);
            }
        }
        log.info("用户执行登出操作");
        return Result.success(null);
    }


    /**
     * 修改密码
     *
     * @return
     */
    //老密码，新密码，检查旧密码，更新数据库密码（md5），令当前用户token失效，跳转回登录页面
    @PutMapping("/changePassword")
    public Result changePassword(@RequestBody UserChangePasswordDTO userChangePasswordDTO) {
        log.info("修改密码：{}", userChangePasswordDTO);

        authService.oldPasswordVerification(userChangePasswordDTO);
        // TODO:记得检查前端在返回后有没有跳转回登录页面
        return Result.success();
    }

}




