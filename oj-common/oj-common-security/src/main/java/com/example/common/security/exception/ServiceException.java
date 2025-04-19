package com.example.common.security.exception;

import com.example.common.core.enums.ResultCode;
import lombok.Getter;

//业务异常类
@Getter
public class ServiceException extends RuntimeException {

    private final ResultCode resultCode;

    public ServiceException(ResultCode resultCode) {
        this.resultCode = resultCode;
    }

}
