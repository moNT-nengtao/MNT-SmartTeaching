package com.smartteaching.common.exception;

/**
 * @ClassName TokenInvalidException
 * @Description Token无效异常
 * @Author MNT
 * @Date 2026/8/14 10:56
 **/
public class TokenInvalidException extends BaseException {

    public TokenInvalidException() {
        super("Token无效或已过期");
    }

    public TokenInvalidException(String msg) {
        super(msg);
    }
}