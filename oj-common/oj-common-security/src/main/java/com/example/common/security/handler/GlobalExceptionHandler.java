package com.example.common.security.handler;

import com.example.common.security.exception.ServiceException;
import com.example.common.core.domain.Result;
import com.example.common.core.enums.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

//全局异常拦截器
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    /**
     * 请求方式不支持
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<?> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e,
                                                         HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("资源：{}，不支持请求方式:{}", requestURI, e.getMethod());
        return Result.fail(ResultCode.ERROR);
    }

    /**
     * 拦截参数校验异常
     */
    @ExceptionHandler(BindException.class)
    public Result<?> handleBindException(BindException e, HttpServletRequest request) {
        log.error("请求资源:{}, 发生异常:{}",request.getRequestURI(), e.getMessage());
        if(e.getMessage().contains("Failed to convert")) {
            return Result.fail(ResultCode.FAILED_PARAMS_VALIDATE);
        }
        String message = join(e.getAllErrors(),
                DefaultMessageSourceResolvable::getDefaultMessage,",");
        return Result.fail(ResultCode.FAILED_PARAMS_VALIDATE.getCode(),message);
    }

    /**
     * 拼接所有异常信息
     */
    public <E> String join(Collection<E> collection, Function<E,String> function,
                           CharSequence delimiter) {
        if(CollectionUtils.isEmpty(collection)) {
            return "";
        }
        return collection.stream().map(function).filter(Objects::nonNull).collect(
                Collectors.joining(delimiter));
    }

    /**
     * 拦截运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<?> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求资源:{}，发生运行时异常：{}", requestURI, e.getMessage());
        if(e.getMessage().contains("Duplicate entry")) {
            return Result.fail(ResultCode.FAILED_ALREADY_EXISTS);
        }
        return Result.fail(ResultCode.ERROR);
    }

    /**
     * 拦截业务异常
     */
    @ExceptionHandler(ServiceException.class)
    public Result<?> handleServiceException(ServiceException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求资源:{}，发生业务异常：{}", requestURI, e.getResultCode().getMsg());
        return Result.fail(e.getResultCode());
    }



    /**
     * 拦截系统异常
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求资源:{}，发生异常：{}", requestURI, e.getMessage());
        return Result.fail(ResultCode.ERROR);
    }
}
