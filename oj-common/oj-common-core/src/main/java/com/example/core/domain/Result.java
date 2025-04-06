package com.example.core.domain;

import com.example.core.enums.ResultCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 统一返回结果类
 * @param <T>
 */
@Data
@Schema(name = "统一返回结果类")
public class Result <T>{

    @Schema(name = "响应状态码")
    private int code; //定义一些固定的code 前后端协调 常量集合 也就死枚举类型

    @Schema(name = "响应状态信息")
    private String msg; //通常是code的辅助说明 一个code对应一个msg

    @Schema(name = "响应数据")
    private T data; //使用泛型

    //组装结果通用方法
    public static<T> Result<T> assembleResult(T data, ResultCode resultCode) {
        Result<T> result = new Result<>();
        result.setCode(resultCode.getCode());
        result.setMsg(resultCode.getMsg());
        result.setData(data);
        return result;
    }

    //失败 需要状态码和信息
    public static<T> Result<T> fail(ResultCode resultCode) {
        return assembleResult(null, resultCode);
    }

    //失败 未知错误 默认错误状态码和信息
    public static<T> Result<T> fail() {
        return assembleResult(null,ResultCode.FAILED);
    }

    //成功 不需要返回数据
    public static<T> Result<T> ok() {
        return assembleResult(null, ResultCode.SUCCESS);
    }

    //成功 需要返回数据
    public static<T> Result<T> ok(T data) {
        return assembleResult(data, ResultCode.SUCCESS);
    }

}
