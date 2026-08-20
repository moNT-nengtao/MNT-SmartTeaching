package com.smartteaching.common.exception;

import com.smartteaching.common.constant.MessageConstant;
import com.smartteaching.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获自定义业务异常 BaseException
     */
    @ExceptionHandler(BaseException.class)
    public Result<String> baseExceptionHandler(BaseException ex) {
        log.error("异常信息：{}", ex.getMessage());
        ex.printStackTrace();
        return Result.error(ex.getMessage());
    }

    /**
     * 捕获运行时业务异常（比如直接throw new RuntimeException("xxx")）
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<String> runtimeExceptionHandler(RuntimeException ex) {
        log.error("异常信息：{}", ex.getMessage(), ex);
        ex.printStackTrace();
        return Result.error(ex.getMessage());
    }

    /**
     * 唯一约束重复
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public Result handleDuplicateKeyException(DuplicateKeyException ex) {
        String msg = ex.getMessage();
        Pattern pattern = Pattern.compile("Duplicate entry '(.*?)'");
        Matcher matcher = pattern.matcher(msg);
        if (matcher.find()) {
            String account = matcher.group(1);
            return Result.error(account + MessageConstant.ALREADY_EXISTS);
        }
        return Result.error(MessageConstant.UNKNOWN_ERROR);
    }

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Result sqlExceptionHandler(SQLIntegrityConstraintViolationException ex){
        log.error("SQL约束异常",ex);
        return Result.error(MessageConstant.UNKNOWN_ERROR);
    }

    /**
     * 全局兜底，所有其它未知异常
     */
    @ExceptionHandler(Exception.class)
    public Result<String> exceptionHandler(Exception ex) {
        log.error("系统未知异常：", ex);
        ex.printStackTrace();
        return Result.error(MessageConstant.UNKNOWN_ERROR);
    }

}
