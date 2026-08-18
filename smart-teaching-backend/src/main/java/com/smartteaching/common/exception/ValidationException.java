package com.smartteaching.common.exception;

/**
 * 参数校验异常（如角色无效、班级ID缺失等）
 */
public class ValidationException extends BaseException {

    public ValidationException() {
        super("参数校验失败");
    }

    public ValidationException(String msg) {
        super(msg);
    }
}