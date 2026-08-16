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
     * 捕获业务异常
     *
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result<String> baseExceptionHandler(BaseException ex) {
        log.error("异常信息：{}", ex.getMessage());
        ex.printStackTrace(); //输出到控制台
        return Result.error(ex.getMessage());
    }

    /**
     * 全局默认异常处理
     *
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result<String> exceptionHandler(Exception ex) {
        log.error("异常信息：{}", ex.getMessage());
        ex.printStackTrace(); //输出到控制台
        return Result.error(MessageConstant.UNKNOWN_ERROR);
    }

    /**
     *处理新增员工时重复的sql异常
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
    public Result exceptionHandler(SQLIntegrityConstraintViolationException ex){
        return Result.error(MessageConstant.UNKNOWN_ERROR);
    }


}

