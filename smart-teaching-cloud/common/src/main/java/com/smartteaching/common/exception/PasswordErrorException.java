package com.smartteaching.common.exception;

/**
 * @ClassName PasswordErrorException
 * @Description 密码错误异常
 * @Author MNT
 * @Date 2026/8/14 16:37
 **/
public class PasswordErrorException extends BaseException {

    public PasswordErrorException() {
    }

    public PasswordErrorException(String msg) {
        super(msg);
    }

}
