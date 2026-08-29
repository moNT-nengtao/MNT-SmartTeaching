package com.smartteaching.common.exception;

/**
 * @ClassName UserAlreadyExistsException
 * @Description 用户已存在异常（用户名、邮箱、手机号重复）
 * @Author MNT
 * @Date 2026/8/14 08:42
 **/
public class UserAlreadyExistsException extends BaseException {

    public UserAlreadyExistsException() {
        super("用户已存在");
    }

    public UserAlreadyExistsException(String msg) {
        super(msg);
    }
}