package com.smartteaching.common.exception;

/**
 * @ClassName BaseException
 * @Description 业务异常
 * @Author MNT
 * @Date 2026/8/14 15:02
 **/
public class BaseException extends RuntimeException {

    public BaseException() {
    }

    public BaseException(String msg) {
        super(msg);
    }

}
