package com.example.common.security.exception;

import com.example.core.enums.ResultCode;
import lombok.Getter;

@Getter
public class ServiceException extends RuntimeException {

    private final ResultCode resultCode;

    public ServiceException(ResultCode resultCode) {
        this.resultCode = resultCode;
    }

}
