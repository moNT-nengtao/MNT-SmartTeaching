package com.smartteaching.service.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.smartteaching.common.constant.MessageConstant;
import com.smartteaching.common.dto.UserChangePasswordDTO;
import com.smartteaching.common.dto.UserLoginDTO;
import com.smartteaching.common.exception.BaseException;
import com.smartteaching.common.exception.PasswordErrorException;
import com.smartteaching.entity.user.User;
import com.smartteaching.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    /**
     * 登录
     * @param userLoginDTO
     * @return
     */
    @Override
    public User login(UserLoginDTO userLoginDTO) {
        String username = userLoginDTO.getUsername();
        String password = userLoginDTO.getPassword();
        String selectedRole = userLoginDTO.getRole();
        //md5加密传入的密码，和库中比对
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        // 查询
        LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(User::getUsername, username).eq(User::getStatus, 1);
        User user = userMapper.selectOne(wrapper);

        if(user == null){
            throw new BaseException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        if (!password.equals(user.getPassword())) {
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }
        if (selectedRole != null && !selectedRole.isBlank() && !selectedRole.equalsIgnoreCase(user.getRole())) {
            throw new BaseException(MessageConstant.ROLE_MISMATCH);
        }
        // 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);

        return user;
    }

    /**
     * 获取当前登录用户信息
     * @param username
     * @return
     */
    @Override
    public User getUserByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return userMapper.selectOne(wrapper);
    }

    /**
     * 效验旧密码
     */
    @Override
    @Transactional(rollbackFor = BaseException.class)
    public void oldPasswordVerification(UserChangePasswordDTO userChangePasswordDTO) {

        //1. 获取当前登录用户
        User loginUser = getCurrentLoginUser();

        //2. 密码对比
        String oldPwdMd5 = DigestUtils.md5DigestAsHex(userChangePasswordDTO.getOldPassword().getBytes());
        if (!oldPwdMd5.equals(loginUser.getPassword())) {
            throw new BaseException(MessageConstant.PASSWORD_ERROR);
        }

        //3. 修改密码
        String newPwd = DigestUtils.md5DigestAsHex(userChangePasswordDTO.getNewPassword().getBytes());
        LambdaUpdateWrapper<User> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(User::getId, loginUser.getId()).set(User::getPassword, newPwd);
        //修改数据库
        userMapper.update(null, updateWrapper);

        //4.redis版本号+1，该用户所有旧JWT失效
        // TODO：后期需要补：定时任务清理僵尸 key，不然redis持续增多（一个用户一条）
        String key = "user:token:version:" + loginUser.getId();
        stringRedisTemplate.opsForValue().increment(key);

    }


    /**
     * 抽取工具方法：从Security上下文拿到当前登录用户
     * TODO: 避免直接强转Principal，防止ClassCastException
     */
    private User getCurrentLoginUser(){

        UsernamePasswordAuthenticationToken authToken =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        // principal是spring security内置User对象，获取用户名
        String username = authToken.getName();
        // 根据用户名查数据库，拿到自己的User实体
        LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(wrapper);

        if(user == null){
            throw new BaseException("登录用户不存在");
        }
        return user;
    }

}
