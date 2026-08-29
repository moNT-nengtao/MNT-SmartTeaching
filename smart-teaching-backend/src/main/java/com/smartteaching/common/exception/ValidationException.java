package com.smartteaching.common.exception;

/**
 * @ClassName ValidationException
 * @Description 参数校验异常（如角色无效、班级ID缺失等）
 * @Author MNT
 * @Date 2026/8/14 14:51
 **/
public class ValidationException extends BaseException {

    public ValidationException() {
        super("参数校验失败");
    }

    public ValidationException(String msg) {
        super(msg);
    }
}