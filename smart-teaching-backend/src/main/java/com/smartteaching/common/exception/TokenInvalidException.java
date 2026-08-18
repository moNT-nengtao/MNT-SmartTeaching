package com.smartteaching.common.exception;

/**
 * Token无效异常
 */
public class TokenInvalidException extends BaseException {

    public TokenInvalidException() {
        super("Token无效或已过期");
    }

    public TokenInvalidException(String msg) {
        super(msg);
    }
}