package com.smartteaching.common.exception;

/**
 * 用户已存在异常（用户名、邮箱、手机号重复）
 */
public class UserAlreadyExistsException extends BaseException {

    public UserAlreadyExistsException() {
        super("用户已存在");
    }

    public UserAlreadyExistsException(String msg) {
        super(msg);
    }
}